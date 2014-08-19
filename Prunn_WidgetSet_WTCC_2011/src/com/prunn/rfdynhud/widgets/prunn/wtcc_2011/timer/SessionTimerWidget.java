package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.timer;

import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionLimit;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
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
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */
public class SessionTimerWidget extends Widget
{
	
	private final EnumValue<YellowFlagState> SCState = new EnumValue<YellowFlagState>();
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final EnumValue<GamePhase> oldGamePhase = new EnumValue<GamePhase>();
    private final IntValue LapsLeft = new IntValue();
    private final BoolValue sectorYellowFlag = new BoolValue();
    private final BoolValue GreenFlag = new BoolValue();
    private final BoolValue FinishFlag = new BoolValue();
    private final FloatValue sessionTime = new FloatValue(-1F, 0.1F);
    private DrawnString dsSession = null;
    private DrawnString dsSC = null;
    private String strlaptime = "";
    private TextureImage2D texSC = null;
    private TextureImage2D texMisc = null;
    private final StringValue strLaptime = new StringValue( "" );
    private ImagePropertyWithTexture imgBG = new ImagePropertyWithTexture( "imgBG", "prunn/WTCC/timer/timer_bg.png" );
    private ImagePropertyWithTexture imgBGYellow = new ImagePropertyWithTexture( "imgBGYellow", "prunn/WTCC/timer/timer_yellow.png" );
    private ImagePropertyWithTexture imgBGGreen = new ImagePropertyWithTexture( "imgBGGreen", "prunn/WTCC/timer/timer_green.png" );
    private final ImagePropertyWithTexture imgBGFinish = new ImagePropertyWithTexture( "imgBGFinished", "prunn/WTCC/timer/timer_finished.png" );
    private final ImagePropertyWithTexture imgSC = new ImagePropertyWithTexture( "imgSC", "prunn/WTCC/timer/yellow_flag.png" );
    private final ImagePropertyWithTexture imgMisc = new ImagePropertyWithTexture( "imgMisc", "prunn/WTCC/timer/misc_flag.png" );
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    private final ColorProperty fontColor1 = new ColorProperty( "fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    private final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    private BooleanProperty useLapLeft = new BooleanProperty("show laps left", false);
    private long visibleEnd;
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        LapsLeft.reset();
    	sectorYellowFlag.reset();
    	SCState.reset();
        gamePhase.reset();
        String cpid = "Y29weXJpZ2h0QFBydW5uMjAxMQ";
        if(!isEditorMode)
            log(cpid);
        
    }
    public void onSessionStarted(SessionType sessionType, LiveGameData gameData, boolean isEditorMode)
    {
        super.onSessionStarted(sessionType, gameData, isEditorMode);
        gamePhase.reset();
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
        
        dsSession = drawnStringFactory.newDrawnString( "dsSession",width*55/100 , height/4 - fh/2 + fontyoffset.getValue(), Alignment.CENTER, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        imgBG.updateSize( width*48/100, height*47/100, isEditorMode );
        imgBGYellow.updateSize( width*48/100, height*47/100, isEditorMode );
        imgBGGreen.updateSize( width*48/100, height*47/100, isEditorMode );
        imgBGFinish.updateSize( width*48/100, height*47/100, isEditorMode );
        texSC = imgSC.getImage().getScaledTextureImage( width, height, texSC, isEditorMode );
        texMisc = imgMisc.getImage().getScaledTextureImage( width, height, texMisc, isEditorMode );
        dsSC = drawnStringFactory.newDrawnString( "dsSC", width*62/100, height*75/100 - fh/2 + fontyoffset.getValue(), Alignment.CENTER, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor() );
        
    }
    
    protected Boolean updateVisibility(LiveGameData gameData, boolean isEditorMode)
    {
        
        super.updateVisibility(gameData, isEditorMode);
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        SCState.update(scoringInfo.getYellowFlagState());
        sectorYellowFlag.update(scoringInfo.getSectorYellowFlag(scoringInfo.getViewedVehicleScoringInfo().getSector()));
        gamePhase.update(scoringInfo.getGamePhase());
        
        if(gamePhase.getValue() == GamePhase.GREEN_FLAG && oldGamePhase.getValue() == GamePhase.BEFORE_SESSION_HAS_BEGUN && gamePhase.hasChanged())
        {
            visibleEnd = scoringInfo.getSessionNanos() + 5000000000l;
        }
                
        oldGamePhase.update(scoringInfo.getGamePhase());
        
        if(scoringInfo.getSessionNanos() < visibleEnd)
            GreenFlag.update( true );
        else
            GreenFlag.update( false );
        
        if(gamePhase.getValue() == GamePhase.SESSION_OVER)//scoringInfo.getSessionNanos() < visibleEndFinish
            FinishFlag.update( true );
        else
            FinishFlag.update( false );
        
        if((GreenFlag.hasChanged() || FinishFlag.hasChanged()) && !isEditorMode)
            forceCompleteRedraw(true);
        
        if((SCState.hasChanged() || sectorYellowFlag.hasChanged() || oldGamePhase.hasChanged()) && !isEditorMode)
            forceCompleteRedraw(true);
        
        if( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP )
            return false;
        if( scoringInfo.getGamePhase() == GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN && scoringInfo.getEndTime() <= scoringInfo.getSessionTime() )
            return false;
        
        return true;
        
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        /*if(sectorYellowFlag.getValue() || (SCState.getValue() != YellowFlagState.NONE && SCState.getValue() != YellowFlagState.RESUME))
            texture.clear( imgBGYellow.getTexture(), offsetX + width*30/100, offsetY, false, null );
        else*/
            if(SCState.getValue() == YellowFlagState.RESUME || GreenFlag.getValue())
                texture.clear( imgBGGreen.getTexture(), offsetX + width*30/100, offsetY, false, null );
            else
                if(gamePhase.getValue() == GamePhase.SESSION_OVER)
                    texture.clear( imgBGFinish.getTexture(), offsetX + width*30/100, offsetY, false, null );
                else
                    texture.clear( imgBG.getTexture(), offsetX + width*30/100, offsetY, false, null );
        
        if((SCState.getValue() != YellowFlagState.NONE && SCState.getValue() != YellowFlagState.RESUME && scoringInfo.getViewedVehicleScoringInfo().getFinishStatus() != FinishStatus.FINISHED) || isEditorMode )
            texture.drawImage( texSC, offsetX, offsetY, true, null );
        else
            if((sectorYellowFlag.getValue() || (SCState.getValue() != YellowFlagState.NONE && SCState.getValue() != YellowFlagState.RESUME)) && scoringInfo.getViewedVehicleScoringInfo().getFinishStatus() != FinishStatus.FINISHED)
                texture.drawImage( texSC, offsetX, offsetY, true, null ); 
            else
                if(gamePhase.getValue() == GamePhase.RECONNAISSANCE_LAPS)
                    texture.drawImage( texMisc, offsetX, offsetY, true, null );
        
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        String strSC = "";
        
        if (scoringInfo.getSessionType().isRace() && scoringInfo.getViewedVehicleScoringInfo().getSessionLimit() == SessionLimit.LAPS && gamePhase.getValue() != GamePhase.RECONNAISSANCE_LAPS)
    	{
            if(useLapLeft.getValue())
                LapsLeft.update(scoringInfo.getMaxLaps() - scoringInfo.getLeadersVehicleScoringInfo().getLapsCompleted());
            else
                LapsLeft.update(scoringInfo.getLeadersVehicleScoringInfo().getCurrentLap());
            
            if(scoringInfo.getLeadersVehicleScoringInfo().getCurrentLap() == scoringInfo.getMaxLaps())
                strlaptime = "LAST LAP";
            else
                if(scoringInfo.getLeadersVehicleScoringInfo().getCurrentLap() > scoringInfo.getMaxLaps())
	    	    strlaptime = "";
                else
                    if ( needsCompleteRedraw || LapsLeft.hasChanged() )
                        strlaptime = LapsLeft.getValueAsString() + " / " + scoringInfo.getMaxLaps();
	    
	    	
		}
    	else // Test day only
    		if(scoringInfo.getSessionType().isTestDay())
    		    strlaptime = scoringInfo.getViewedVehicleScoringInfo().getLapsCompleted() + "  /  ";
    		else // any other timed session (Race, Qualify, Practice)
	    	{
    		    
		    	sessionTime.update(scoringInfo.getSessionTime());
	    		float endTime = scoringInfo.getEndTime();
	    		
	    		//logCS(gamePhase.getValue(), scoringInfo.getLeadersVehicleScoringInfo().getFinishStatus(),endTime , sessionTime.getValue());
                    
	    		if ( needsCompleteRedraw || sessionTime.hasChanged() )
		        {
	    		    if(gamePhase.getValue() == GamePhase.GREEN_FLAG && endTime <= sessionTime.getValue() && scoringInfo.getLeadersVehicleScoringInfo().getFinishStatus() != FinishStatus.FINISHED)
	    		        strlaptime = "0:00";
	    		    else
	    		        if(gamePhase.getValue() == GamePhase.SESSION_OVER || (endTime <= sessionTime.getValue() && gamePhase.getValue() != GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN ) )
			                strlaptime = "";
	    		        else
            			    if(gamePhase.getValue() == GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN && endTime <= sessionTime.getValue())
    		        		    strlaptime = "0:00";
    		        		else
    		        		{
    		        		    
    		        		    strlaptime = TimingUtil.getTimeAsString(endTime - sessionTime.getValue(), true, false);
    		        	
            		        	if (strlaptime.charAt( 0 ) == '0')
            		        	    strlaptime = strlaptime.substring( 1 );
            		        	if (strlaptime.charAt( 0 ) == '0')
                                    strlaptime = strlaptime.substring( 2 );
            		        	if (strlaptime.charAt( 0 ) == '0')
                                    strlaptime = strlaptime.substring( 1 );
    		        		}
		        }
	    		
	    	
	    	}
        
        strLaptime.update( strlaptime );
        
        if ( needsCompleteRedraw || ( clock.c() && strLaptime.hasChanged() ) )
        {
            String sector;    
            
            if(scoringInfo.getViewedVehicleScoringInfo().getSector() == 1)
                sector = "1st SECTOR";
            else
                if(scoringInfo.getViewedVehicleScoringInfo().getSector() == 2)
                    sector = "2nd SECTOR";
                else
                    sector = "3rd SECTOR";
                    
            
            if(!FinishFlag.getValue())
                dsSession.draw( offsetX, offsetY, strlaptime,fontColor2.getColor(), texture );
            
            if((SCState.getValue() != YellowFlagState.NONE && SCState.getValue() != YellowFlagState.RESUME) || isEditorMode)
                strSC = "SAFETY CAR";//1st SECTOR
            else
                if((sectorYellowFlag.getValue() || (SCState.getValue() != YellowFlagState.NONE && SCState.getValue() != YellowFlagState.RESUME)) && scoringInfo.getViewedVehicleScoringInfo().getFinishStatus() != FinishStatus.FINISHED)
                    strSC = sector;
                /*else
                    if(gamePhase.getValue() == GamePhase.RECONNAISSANCE_LAPS)
                        strSC = "PIT LANE CLOSES";*/
                    else
                        strSC = "";
            
            dsSC.draw( offsetX, offsetY, strSC, texture );
            
        }  
        
      
        
    }
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        writer.writeProperty( wtcc_2011_Font, "timeFont" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( fontyoffset, "" );
        writer.writeProperty( useLapLeft, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( fontyoffset ) );
        else if ( loader.loadProperty( useLapLeft ) );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Font" );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty( useLapLeft );
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
    
    public SessionTimerWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 15.0f, 5.0f );
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
        
    }
}
