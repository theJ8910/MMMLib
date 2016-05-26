package net.theJ89.forge;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import net.theJ89.http.HTTP;
import net.theJ89.http.HTTPResponse;
import net.theJ89.mmm.SideCompat;
import net.theJ89.util.Misc;

/**
 * ForgeIndex is a fully static class that scrapes the Forge Files index for data on Forge versions and downloads.
 * As parse() works through the index pages, it calls methods in a user provided ForgeIndexContentHandler.
 * You can override the methods in the content handler to perform actions at each step of the process.
 * For example, as ForgeIndex calls handler.forge(), the ForgeVersionsUpdater class creates corresponding database entries.
 */
public class ForgeIndex {
    private static final String  FORGE_INDEX_URL             = "http://files.minecraftforge.net/";
    
    private static final String  SEL_MC_VERSIONS             = "div.versions > ul.links > li.li-version-list > div > ul > li";
    private static final String  SEL_LATEST                  = "div.downloads > div.download > div.title:has(i.promo-LATEST) > small";
    private static final String  SEL_RECOMMENDED             = "div.downloads > div.download > div.title:has(i.promo-RECOMMENDED) > small";
    private static final String  SEL_FORGE_VERSIONS          = "table#downloadsTable tr:has(td)";
    private static final String  SEL_FORGE_VERSION_NAME      = "td:eq(0) > ul > li";
    private static final String  SEL_FORGE_VERSION_TIME      = "td:eq(1)";
    private static final String  SEL_FORGE_VERSION_DOWNLOADS = "td:eq(2) > ul > li";
    
    private static final Pattern RE_MC_FORGE_VERSIONS        = Pattern.compile( "(.+?) - (.+?)" );
    
    //Example date-time format used by Forge index: "05/16/2016 04:28:01 PM"
    private static final DateTimeFormatter DATETIME_FORMATTER    = DateTimeFormatter.ofPattern( "MM/dd/yyyy hh:mm:ss a" );
    private static final ZoneId            ZONEID_CANADA_EASTERN = ZoneId.of( "Canada/Eastern" );
    
    /**
     * Parse the Forge Files index using the default URL and given handler
     * @param handler
     * @throws Exception
     */
    public static void parse( final ForgeIndexContentHandler handler ) throws Exception {
        parse( FORGE_INDEX_URL, handler );
    }
    
    /**
     * Parse the Forge Files index using the given URL and handler
     * @param url
     * @param handler
     * @throws Exception
     */
    public static void parse( final String url, final ForgeIndexContentHandler handler ) throws Exception {
        //Grab an index for an unknown version of Minecraft
        Document doc = Jsoup.connect( url ).get();
        
        //Iterate over available indices, each corresponding to a version of Minecraft
        Elements versions = doc.select( SEL_MC_VERSIONS );
        for( Element version : versions ) {
            String   name;
            Document index;
            
            //Current page is the index for this version
            if( version.hasClass( "li-version-list-current" ) ) {
                name  = version.text().trim();
                index = doc;
            //Index for this version is on another page.
            } else {
                Element link = version.getElementsByTag( "a" ).first();
                name = link.text().trim();
                
                //Note use of .absUrl() here and in other locations in this file.
                //The HTML standard permits the use of relative URLs to describe a resource located relative to the location of the document containing the relative URL.
                //Unfortunately, an absolute URL is required to make an HTTP request, and as it turns out some links provided by the Forge Files index use relative URLs.
                //Therefore we must ensure the URL is absolute with Jsoup's .absUrl() method:
                index = Jsoup.connect( link.absUrl( "href" ) ).get();
            }
            
            //Start parsing the index for this Minecraft version
            parseIndex( name, index, handler );
        }
    }
    
    private static void parseIndex( final String mcversion, final Document doc, final ForgeIndexContentHandler handler ) throws Exception {
        //Determine latest and recommended Forge versions for this version of Minecraft (if any)
        String latest      = parsePromotedVersion( doc, SEL_LATEST );
        String recommended = parsePromotedVersion( doc, SEL_RECOMMENDED );
        
        //Signal to handler we're starting to parse Forge versions for this version of Minecraft
        MinecraftVersion mv = new MinecraftVersion( mcversion, latest, recommended );
        handler.startMinecraft( mv );
        
        for( Element version : doc.select( SEL_FORGE_VERSIONS ) ) {
            String name = version.select( SEL_FORGE_VERSION_NAME ).first().textNodes().get(0).text().trim();
            
            String strtime = version.select( SEL_FORGE_VERSION_TIME ).first().text();
            ZonedDateTime time =
                LocalDateTime.parse( strtime, DATETIME_FORMATTER )  //Parse the scraped date/time string to a local date/time. The provided date/time doesn't specify an offset.
                .atZone( ZONEID_CANADA_EASTERN );                   //I'm assuming the times are relative to Eastern Canada based on the location of the Forge Files server.
            
            //Signal to handler we've parsed basic details for a version of Forge
            handler.forge( new ForgeVersion( name, time ) );
            
            //Parse downloads
            for( Element download : version.select( SEL_FORGE_VERSION_DOWNLOADS ) )
                parseDownload( download, handler );
        }
        
        //Signal to handler we're finished with the index for this version of Minecraft
        handler.endMinecraft( mv );
    }
    
    private static void parseDownload( final Element download, final ForgeIndexContentHandler handler ) throws Exception {
        ForgeDownload fd = new ForgeDownload();
        
        //Primary information about the download.
        //This is an <a> element containing the type and URL of the download.
        Element primary = download.child( 0 );
        
        //Determine download type.
        //We're only interested in the universal, client, and server downloads.
        //Ignore all other downloads.
        String strType = primary.text().trim().toLowerCase();
        switch( strType ) {
        case "universal":
            fd.setType( SideCompat.UNIVERSAL );
            break;
        case "client":
            fd.setType( SideCompat.CLIENT );
            break;
        case "server":
            fd.setType( SideCompat.SERVER );
            break;
        default:
            return;
        }
        
        //Get the URL for the download.
        fd.setURL( primary.absUrl( "href" ) );

        //Each download in the index provides additional information in an extended info element.
        //We can (usually) get MD5 and SHA1 hashes for the file by parsing this information.
        Element extended = download.child( 2 );
        for( Element strong : extended.getElementsByTag( "strong" ) ) {
            Node n = strong.nextSibling();
            if( !(n instanceof TextNode) )
                continue;
            
            String header  = strong.text().trim();
            String content = ((TextNode)n).text().trim();
            if( Objects.equals( header, "MD5:" ) && Misc.isValidMD5( content ) ) {
                fd.setMD5( content );
            } else if( Objects.equals( header, "SHA1:" ) && Misc.isValidSHA1( content ) ) {
                fd.setSHA1( content );
            }
        }
        
        //The download URL we parsed earlier may be an adfoc.us link.
        //We need the direct download link for automation purposes.
        //Thankfully, the Forge Files website provides these in a link in the extended info for the download.
        Element link = extended.getElementsByTag( "a" ).first();
        if( link != null )
            fd.setURL( link.absUrl( "href" ) );
        
        //The Forge Files website doesn't provide file sizes on the index itself.
        //We'll need to perform a HEAD request on the download to get this information (without actually downloading the file).
        //Making several HEAD requests is spammy and can potentially take a long time, so the content handler has an opportunity to skip this step
        //in the case the file size isn't desired or is already known.
        if( handler.startDownload( fd ) ) {
            HTTPResponse resp = HTTP.head( new URL( fd.getURL() ) );
            if( resp.ok() ) { fd.setSize( resp.getContentLength() ); }
            else            { System.err.println( String.format( "Warning: HTTP %d for URL \"%s\".", resp.getStatus(), fd.getURL() ) ); }
        }
        
        //Signal to handler we're finished with this download
        handler.endDownload( fd );
    }
    
    /**
     * Returns the latest / recommended version name.
     * @param doc - Parsed forge index page.
     * @param selector - Either SEL_LATEST or SEL_RECOMMENDED.
     * @return
     */
    private static String parsePromotedVersion( final Document doc, final String selector ) {
        //Find the element containing the latest / recommended version text
        Element match = doc.select( selector ).first();
        if( match == null )
            return null;
        
        //Extract the Forge version from the string.
        //This can occur in two forms: "mcver - forgever" or simply "forgever".
        //We use a regular expression here to detect and extract the forge version from the first form.
        String text = match.text().trim();
        Matcher m = RE_MC_FORGE_VERSIONS.matcher( text );
        if( m.matches() )
            return m.group( 2 );
        return text;
    }
}
