
package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.datatest;

import java.awt.Font;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

/**
 * 
 * 
 * @author Prunn
 */
public class RandomDataWidget extends Widget
{
    private DrawnString dsData1 = null;
    private DrawnString dsData2 = null;
    private DrawnString dsData3 = null;
    private DrawnString dsData4 = null;
    private DrawnString dsData5 = null;
    private DrawnString dsData6 = null;
    private DrawnString dsData7 = null;
    private DrawnString dsData8 = null;
    
    
    private final EnumValue<YellowFlagState> data1 = new EnumValue<YellowFlagState>();
    //private final BoolValue data1 = new BoolValue( );
    private final BoolValue data2 = new BoolValue();
    private final BoolValue data3 = new BoolValue();
    private final BoolValue data4 = new BoolValue();
    private final StringValue data5 = new StringValue();
    private final StringValue data6 = new StringValue();
    private final StringValue data7 = new StringValue();
    private final FloatValue data8 = new FloatValue( -1f, 0.1f );
    long LapSystemTime = 0;
    
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        
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
    	
        dsData1 = drawnStringFactory.newDrawnString( "dsData1", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  "" );
        dsData2 = drawnStringFactory.newDrawnString( "dsData2", 0, 20, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        dsData3 = drawnStringFactory.newDrawnString( "dsData3", 0, 40, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        dsData4 = drawnStringFactory.newDrawnString( "dsData4", 0, 60, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        dsData5 = drawnStringFactory.newDrawnString( "dsData5", 0, 80, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        dsData6 = drawnStringFactory.newDrawnString( "dsData6", 0, 100, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        dsData7 = drawnStringFactory.newDrawnString( "dsData7", 0, 120, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        dsData8 = drawnStringFactory.newDrawnString( "dsData8", 0, 140, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null,  ""  );
        
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        data1.update( gameData.getScoringInfo().getYellowFlagState() );
        String dataname1 = "getYellowFlagState: ";
        data2.update(gameData.getScoringInfo().getSectorYellowFlag(1));
        String dataname2 = "getSectorYellowFlag1: ";
        data3.update(gameData.getScoringInfo().getSectorYellowFlag(2));
        String dataname3 = "getSectorYellowFlag2: ";
        data4.update(gameData.getScoringInfo().getSectorYellowFlag(3));
        String dataname4 = "getSectorYellowFlag3: ";
        data5.update( gameData.getScoringInfo().getViewedVehicleScoringInfo().getDriverName() ); 
        String dataname5 = "getDriverName: ";
        data6.update(gameData.getScoringInfo().getViewedVehicleScoringInfo().getDriverNameShort());
        String dataname6 = "getDriverNameShort: ";
        data7.update( "" );
        String dataname7 = "getTeamHeadquarters:";
        data8.update( 0 );
        String dataname8 = "getTrackLength: " ;
        
       
        
        if ( needsCompleteRedraw || ( clock.c() && data1.hasChanged()) )
        {
            dsData1.draw( offsetX + 30, offsetY, dataname1 + data1.getValue(), texture );
             
        } //+ data2.getValueAsString()
        if ( needsCompleteRedraw || ( clock.c() && data2.hasChanged()) )
        {
            dsData2.draw( offsetX + 30, offsetY, dataname2 + data2.getValueAsString(), texture );
             
        }
        if ( needsCompleteRedraw ||  (clock.c() && data3.hasChanged()) )
        {
            dsData3.draw( offsetX + 30, offsetY, dataname3 + data3.getValueAsString(), texture );
             
        }
        if ( needsCompleteRedraw || ( clock.c() && data4.hasChanged()) )
        {
            dsData4.draw( offsetX + 30, offsetY, dataname4 + data4.getValueAsString(), texture );
             
        }
        if ( needsCompleteRedraw || ( clock.c() && data5.hasChanged()) )
        {
            dsData5.draw( offsetX + 30, offsetY, dataname5 + data5.getValue()+"0end", texture );
             
        }
        if ( needsCompleteRedraw || ( clock.c() && data6.hasChanged()) )
        {
            LapSystemTime = System.currentTimeMillis(); 
            dsData6.draw( offsetX + 30, offsetY, dataname6 + data6.getValue()+"0end", texture );
             
        }
        if ( needsCompleteRedraw || ( clock.c() && data7.hasChanged()) )
        {
            dsData7.draw( offsetX + 30, offsetY, dataname7 + data7.getValue()+"0end", texture );
             
        }
        if ( needsCompleteRedraw || ( clock.c() && data8.hasChanged()) )
        {
            dsData8.draw( offsetX + 30, offsetY, dataname8 + data8.getValueAsString(), texture );
             
        }
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 9, false, true );
    }
    
    public RandomDataWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 20.0f, 30.0f );
    }
}
