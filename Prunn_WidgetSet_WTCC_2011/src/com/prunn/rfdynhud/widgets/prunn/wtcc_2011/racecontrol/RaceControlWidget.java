package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.racecontrol;

import java.awt.Font;
import java.io.IOException;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class RaceControlWidget extends Widget
{
    private DrawnString dsMessage = null;
    private final EnumValue<YellowFlagState> SCState = new EnumValue<YellowFlagState>();
    private int widgetpart = 0;
    private IntValue Penalties[];
    private IntValue Pentotal =  new IntValue();
    private int flaggeddriver = 0;
    private final ImagePropertyWithTexture imgTime = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/race_control.png" );
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    private final ColorProperty fontColor1 = new ColorProperty( "fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    private final ColorProperty fontColor2 = new ColorProperty( "fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME );
    private final DelayProperty visibleTime;
    private long visibleEnd;
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
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
        int numveh = gameData.getScoringInfo().getNumVehicles();
        
        imgTime.updateSize( width, height, isEditorMode );
        
        dsMessage = drawnStringFactory.newDrawnString( "dsMessage", width/2, height/2 - fh/2 + fontyoffset.getValue(), Alignment.CENTER, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        Penalties = new IntValue[numveh];
        for(int i=0;i < numveh;i++)
        { 
            Penalties[i] = new IntValue();
            Penalties[i].update(0);
            Penalties[i].setUnchanged();
        }
        
    }
    protected Boolean updateVisibility(LiveGameData gameData, boolean isEditorMode)
    {
        
        
        super.updateVisibility(gameData, isEditorMode);
        int numveh = gameData.getScoringInfo().getNumVehicles();
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        SCState.update(gameData.getScoringInfo().getYellowFlagState());
        
        if(isEditorMode)
            return true;
        
        if(scoringInfo.getSessionNanos() < visibleEnd)
            return true;
        
        if(SCState.hasChanged() && (SCState.getValue() == YellowFlagState.PENDING || SCState.getValue() == YellowFlagState.LAST_LAP))
        {
            widgetpart = 1;
            visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            return true;
        }
        
        if(scoringInfo.getSessionType().isRace())
        {
            
            int total=0;
            for(int j=0;j < numveh;j++)
            {
                total += scoringInfo.getVehicleScoringInfo( j ).getNumOutstandingPenalties();
            }
            Pentotal.update( total );
           
            if(Pentotal.getValue() > Pentotal.getOldValue() && Pentotal.hasChanged() && Pentotal.getValue() > 0)
            {
               widgetpart = 0;
               visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
               return true;
            }
            else
                Pentotal.hasChanged();
        }
        
        return false;   
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        //texture.clear( imgTitle.getTexture(), offsetX + width*3/100, offsetY, false, null );
        texture.clear( imgTime.getTexture(), offsetX, offsetY, false, null );
     
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int numveh = gameData.getScoringInfo().getNumVehicles();
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if(widgetpart == 1)
        {
            
            if ( needsCompleteRedraw || SCState.getValue() == YellowFlagState.PENDING)
                dsMessage.draw( offsetX, offsetY, "Race Control: SAFETY CAR DEPLOYED", texture );
            else
                if ( needsCompleteRedraw || SCState.getValue() == YellowFlagState.LAST_LAP)
                    dsMessage.draw( offsetX, offsetY, "Race Control: SAFETY CAR IN THIS LAP", texture );
               
            //dsRC.draw( offsetX, offsetY, "Race Control", texture );
        }
        else
            {
                for(int i=0;i < numveh;i++)
                {
                   Penalties[i].update( scoringInfo.getVehicleScoringInfo( i ).getNumOutstandingPenalties() );
                                
                   if(Penalties[i].hasChanged() && Penalties[i].getValue() > 0 )
                       flaggeddriver = i;
                }
                VehicleScoringInfo vsi = gameData.getScoringInfo().getVehicleScoringInfo( flaggeddriver );
                
                if ( needsCompleteRedraw )
                {
                    //dsRC.draw( offsetX, offsetY, "Race Control", texture );
                    dsMessage.draw( offsetX, offsetY, "Race Control: Drive Through Penalty for " + vsi.getDriverName(), texture );
                }
            }
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty(visibleTime, "");
        writer.writeProperty( fontyoffset, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if( loader.loadProperty(visibleTime));
        else if ( loader.loadProperty( fontyoffset ) );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Colors" );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty(visibleTime);
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
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 6, false, true );
        
    }
    
    public RaceControlWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011_Race, -0.0f, 6.7f );
        visibleTime = new DelayProperty("visibleTime", net.ctdp.rfdynhud.properties.DelayProperty.DisplayUnits.SECONDS, 6);
        visibleEnd = 0;
        Penalties = null;
        getBackgroundProperty().setColorValue( "#00000000" );
    }
    
}
