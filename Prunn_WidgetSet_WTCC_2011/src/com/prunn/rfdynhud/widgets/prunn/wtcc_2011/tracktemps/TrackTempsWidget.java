package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.tracktemps;

import java.awt.Font;
import java.io.IOException;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class TrackTempsWidget extends Widget
{
    private DrawnString dsAmbient = null;
    private DrawnString dsHumidity = null;
    private DrawnString dsTrack = null;
    //private DrawnString dsTrackTemp = null;
    
    private IntValue AmbientTemp = new IntValue();
    private IntValue TrackTemp = new IntValue();
    private FloatValue rainingSeverity = new FloatValue();
    protected final StringProperty strTrack = new StringProperty("Track", "Track");
    protected final StringProperty strAmbiant = new StringProperty("Ambiant", "Ambiant Temperature");
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    
    private final ImagePropertyWithTexture imgName = new ImagePropertyWithTexture( "imgName", "prunn/WTCC/weather.png" );
    
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    protected final ColorProperty WhiteFontColor = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    
    
    
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        String cpid = "Y29weXJpZ2h0QFBydW5uMjAxMQ";
        if(!isEditorMode)
            log(cpid);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int fh = TextureImage2D.getStringHeight( "0%C", wtcc_2011_Font );
        
        imgName.updateSize( width, height, isEditorMode );
        
        dsAmbient = drawnStringFactory.newDrawnString( "dsAmbient", width*18/100, height/2 - fh/2 + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), WhiteFontColor.getColor(), null, "" );
        dsTrack = drawnStringFactory.newDrawnString( "dsTrack", width*47/100, height/2 - fh/2 + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), WhiteFontColor.getColor(), null, "" );
        dsHumidity = drawnStringFactory.newDrawnString( "dsHumidity", width*86/100, height/2 - fh/2 + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), WhiteFontColor.getColor(), null, "" );
        
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        texture.clear( imgName.getTexture(), offsetX, offsetY, false, null );
        
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        String units;
        if(gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.METRIC)
            units = "°C";
        else
            units = "°F";
        
        AmbientTemp.update((int)Math.floor(gameData.getWeatherInfo().getAmbientTemperature()));
        TrackTemp.update((int)Math.floor(gameData.getWeatherInfo().getTrackTemperature()));
        rainingSeverity.update(gameData.getWeatherInfo().getRainingSeverity());
        
        if ( needsCompleteRedraw || AmbientTemp.hasChanged())
            dsAmbient.draw( offsetX, offsetY, AmbientTemp.getValueAsString() + units, texture );
        if ( needsCompleteRedraw || TrackTemp.hasChanged())
        {
            //dsHumidity.draw( offsetX, offsetY, "0%", texture );
            dsTrack.draw( offsetX, offsetY, strTrack.getValue() + " " + TrackTemp.getValueAsString() + units , texture );
        }
        if ( needsCompleteRedraw || rainingSeverity.hasChanged())
        {
            int rain = (int)( rainingSeverity.getValue() * 100 );
            dsHumidity.draw( offsetX, offsetY, rain + "%", texture );   
        }
         
        
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( WhiteFontColor, "" );
        writer.writeProperty( strTrack, "" );
        writer.writeProperty( strAmbiant, "" );
        writer.writeProperty( fontyoffset, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( WhiteFontColor ) );
        else if ( loader.loadProperty( strTrack ) );
        else if ( loader.loadProperty( strAmbiant ) );
        else if ( loader.loadProperty( fontyoffset ) );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Colors" );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( WhiteFontColor );
        propsCont.addProperty( strTrack );
        propsCont.addProperty( strAmbiant );
        propsCont.addProperty( fontyoffset );
    }
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        //getFontProperty().setFont( PrunnWidgetSetf1_2011.F1_2011_FONT_NAME );
        getFontProperty().setFont( "Dialog", Font.PLAIN, 6, false, true );
        
    }
    
    public TrackTempsWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 20.6f, 7.8f );
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
    }
    
}
