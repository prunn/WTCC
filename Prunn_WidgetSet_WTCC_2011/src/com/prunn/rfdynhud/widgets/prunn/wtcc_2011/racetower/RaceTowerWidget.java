package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.racetower;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import com.prunn.rfdynhud.plugins.tlcgenerator.StandardTLCGenerator;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class RaceTowerWidget extends Widget
{
    private final ImagePropertyWithTexture imgPos = new ImagePropertyWithTexture( "imgPos", "prunn/WTCC/tower/race_bg.png" );
    private final ImagePropertyWithTexture imgPosFirst = new ImagePropertyWithTexture( "imgPos", "prunn/WTCC/tower/race_bg_first.png" );
    private final ColorProperty fontColor1 = new ColorProperty("fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME);
    private final ColorProperty fontColor2 = new ColorProperty( "fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME );
    protected final FontProperty wtcc_2011_Race_Numbers_Font = new FontProperty("Pos Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_RACE_NUMBERS_TOWER);
    private final DelayProperty visibleTime = new DelayProperty( "visibleTime", DelayProperty.DisplayUnits.SECONDS, 12 );
    private long visibleEnd = 0;
    private DrawnString[] dsPos = null;
    private DrawnString[] dsName = null;
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    private IntProperty fontyoffsetNumbers = new IntProperty("Y Font Offset Pos", 0);
    private IntProperty fontxposoffset = new IntProperty("X Position Font Offset", 0);
    private IntProperty fontxnameoffset = new IntProperty("X Name Font Offset", 0);
    private IntProperty fontxtimeoffset = new IntProperty("X Time Font Offset", 0);
    private final IntProperty numVeh = new IntProperty( "numberOfVehicles", 8 );
    private IntProperty randMulti = new IntProperty("Show Multiplier", 0);
    private final IntValue currentLap = new IntValue();
    private final IntValue drawnCars = new IntValue();
    private final IntValue carsOnLeadLap = new IntValue();
    private short[] positions = null;
    private String[] names = null;
    StandardTLCGenerator gen = new StandardTLCGenerator();
    
    
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        String cpid = "Y29weXJpZ2h0QFBydW5uMjAxMQ";
        if(!isEditorMode)
            log(cpid);
        drawnCars.reset();
        visibleEnd = 0;
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
        int maxNumItems = numVeh.getValue();
        dsPos = new DrawnString[maxNumItems];
        dsName = new DrawnString[maxNumItems];
                
        int fh = TextureImage2D.getStringHeight( "0%C", getFontProperty() );
        int rowHeight = height / maxNumItems;
        
        imgPos.updateSize( width, rowHeight, isEditorMode );
        imgPosFirst.updateSize( width, rowHeight, isEditorMode );
        
        Color whiteFontColor = fontColor2.getColor();
        
        int top = ( rowHeight - fh ) / 2;
        
        for(int i=0;i < maxNumItems;i++)
        { 
            dsPos[i] = drawnStringFactory.newDrawnString( "dsPos", width*29/200 + fontxposoffset.getValue(), top + fontyoffsetNumbers.getValue() + 3, Alignment.CENTER, false, wtcc_2011_Race_Numbers_Font.getFont(), isFontAntiAliased(), fontColor1.getColor() );
            dsName[i] = drawnStringFactory.newDrawnString( "dsName", width*28/100 + fontxnameoffset.getValue(), top + fontyoffset.getValue(), Alignment.LEFT, false, getFont(), isFontAntiAliased(), whiteFontColor );
            
            top += rowHeight;
        }
        
        
    }
    private void clearArrayValues(int maxNumCars)
    {
        positions = new short[maxNumCars];
        names = new String[maxNumCars];
        
        for(int i=0;i<maxNumCars;i++)
        {
            positions[i] = -1;
            names[i] = "";
        }
    }
    private void FillArrayValues(int onLeaderLap, ScoringInfo scoringInfo, boolean isEditorMode)
    {
        int drawncars = Math.min( scoringInfo.getNumVehicles(), numVeh.getValue() );
        
        if(onLeaderLap > numVeh.getValue() || isEditorMode)
            onLeaderLap = numVeh.getValue();
        //
        for(int i=0;i < onLeaderLap;i++)
        {
            int off = i + drawncars - onLeaderLap;
            //if(off < 0)
            
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            positions[off] = vsi.getPlace( false );
            //logCS(off, positions[off], vsi.getPlace( false ));
            names[off] = gen.ShortNameWTCC( vsi.getDriverName() );
            //if(names[off].indexOf( ' ' ) > 0)
                //names[off] = names[off].substring( names[off].indexOf( ' ' ) + 1 );
        }
    }
    
    
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        super.updateVisibility( gameData, isEditorMode );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        currentLap.update( scoringInfo.getLeadersVehicleScoringInfo().getLapsCompleted() );
        
        if( currentLap.hasChanged() && currentLap.getValue() > 0 && (short)( Math.random() * randMulti.getValue()) == 0 || isEditorMode)
        {
            
            if(scoringInfo.getGamePhase() == GamePhase.SESSION_OVER)
                visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos()*3;
            else
                visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            
            clearArrayValues(scoringInfo.getNumVehicles());
            //clearArrayValues(numVeh.getValue());
            FillArrayValues( 1, scoringInfo, isEditorMode);
            if(!isEditorMode)
                forceCompleteRedraw( true );
            
            return true;
            
        }
        
        if(scoringInfo.getSessionNanos() < visibleEnd || isEditorMode)
        {
            //how many on the same lap?
            int onlap = 0;
            for(int j=0;j < scoringInfo.getNumVehicles(); j++)
            {
                if(scoringInfo.getVehicleScoringInfo( j ).getLapsCompleted() == scoringInfo.getLeadersVehicleScoringInfo().getLapsCompleted() )
                    onlap++;
            }
                
            carsOnLeadLap.update( onlap );
            if (carsOnLeadLap.hasChanged() && !isEditorMode )
            {
                FillArrayValues( onlap, scoringInfo, false);
                forceCompleteRedraw( true );
            }
            return true;
        }
        
        
        return false;
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        int maxNumItems = numVeh.getValue();
        int rowHeight = height / maxNumItems;
        int drawncars = Math.min( scoringInfo.getNumVehicles(), maxNumItems );
        //short posOffset;
        
        for(int i=0;i < drawncars;i++)
        {
            //logCS("REDRAWING",positions[i]);
        
            if(positions[i] != -1 || isEditorMode)
            {
                if(positions[i] == 1)
                    texture.clear( imgPosFirst.getTexture(), offsetX, offsetY+rowHeight*i, false, null );
                else
                    texture.clear( imgPos.getTexture(), offsetX, offsetY+rowHeight*i, false, null );
            
            }
        }
        
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        //ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( needsCompleteRedraw)
        {
            //int drawncars = numVeh.getValue() ;
            int drawncars = Math.min( gameData.getScoringInfo().getNumVehicles(), numVeh.getValue() );
            
            for(int i=0;i < drawncars;i++)
            { 
                if(positions[i] != -1)
                    dsPos[i].draw( offsetX, offsetY, String.valueOf(positions[i]),( positions[i] == 1 ) ? fontColor2.getColor() : fontColor1.getColor(), texture );
                else
                    dsPos[i].draw( offsetX, offsetY, "", texture );
                
                dsName[i].draw( offsetX, offsetY, names[i], texture );
                //dsTime[i].draw( offsetX, offsetY, gaps[i], texture );
            }
        }
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( wtcc_2011_Race_Numbers_Font, "" );
        writer.writeProperty( numVeh, "" );
        writer.writeProperty( visibleTime, "visibleTime" );
        writer.writeProperty( randMulti, "ShowMultiplier" );
        writer.writeProperty( fontyoffset, "" );
        writer.writeProperty( fontyoffsetNumbers, "" );
        writer.writeProperty( fontxposoffset, "" );
        writer.writeProperty( fontxnameoffset, "" );
        writer.writeProperty( fontxtimeoffset, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( wtcc_2011_Race_Numbers_Font ) );
        else if ( loader.loadProperty( numVeh ) );
        else if ( loader.loadProperty( visibleTime ) );
        else if ( loader.loadProperty( randMulti ) );
        else if ( loader.loadProperty( fontyoffset ) );
        else if ( loader.loadProperty( fontyoffsetNumbers ) );
        else if ( loader.loadProperty( fontxposoffset ) );
        else if ( loader.loadProperty( fontxnameoffset ) );
        else if ( loader.loadProperty( fontxtimeoffset ) );
    }
    
    @Override
    protected void addFontPropertiesToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Colors and Fonts" );
        
        super.addFontPropertiesToContainer( propsCont, forceAll );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( wtcc_2011_Race_Numbers_Font );
        
        propsCont.addProperty( fontColor2 );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        propsCont.addProperty( numVeh );
        propsCont.addProperty( visibleTime );
        propsCont.addProperty( randMulti );
        propsCont.addGroup( "Font Displacement" );
        propsCont.addProperty( fontyoffset );
        propsCont.addProperty( fontyoffsetNumbers );
        propsCont.addProperty( fontxposoffset );
        propsCont.addProperty( fontxnameoffset );
        propsCont.addProperty( fontxtimeoffset );
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
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 6, false, true );
    }
    
    public RaceTowerWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011_Race, 20.0f, 32.5f );
        
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
        getFontColorProperty().setColor( PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    }
}
