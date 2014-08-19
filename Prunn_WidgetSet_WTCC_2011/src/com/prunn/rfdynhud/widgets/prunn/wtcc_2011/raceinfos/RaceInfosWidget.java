package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.raceinfos;

import java.io.File;
import java.io.IOException;

import com.prunn.rfdynhud.plugins.tlcgenerator.StandardTLCGenerator;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
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
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class RaceInfosWidget extends Widget
{
    public static Boolean isvisible = false;
    public static Boolean visible()
    {
        return isvisible;
    }
    
    private DrawnString dsPos = null;
    private DrawnString dsName = null;
    private DrawnString dsName2 = null;
    private DrawnString dsTeam = null;
    private DrawnString dsTeamW = null;
    private DrawnString dsTitle = null;
    private DrawnString dsTimeC = null;
    private final ImagePropertyWithTexture imgName = new ImagePropertyWithTexture( "imgName", "prunn/WTCC/fastest.png" );
    private final ImagePropertyWithTexture imgTeam = new ImagePropertyWithTexture( "imgTeam", "prunn/WTCC/info.png" );
    private final ImagePropertyWithTexture imgTitleW = new ImagePropertyWithTexture( "imgTitleW", "prunn/WTCC/winner.png" );
    private final ImagePropertyWithTexture imgPit = new ImagePropertyWithTexture( "imgPit", "prunn/WTCC/pitstop.png" );
    private TextureImage2D texManufacturer = null;
    private final ImagePropertyWithTexture imgBMW = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/bmw.png" );
    
    private final FontProperty posFont = new FontProperty("positionFont", PrunnWidgetSet_wtcc_2011.WTCC_2011_POS_FONT_NAME);
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    protected final FontProperty wtcc_2011_Team_Font = new FontProperty("Team Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TEAMS);
    protected final FontProperty wtcc_2011_Times_Font = new FontProperty("Time Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TIMES);
    protected final FontProperty wtcc_2011_Race_Numbers_Font = new FontProperty("Team Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_RACE_NUMBERS);
    protected final ColorProperty fontColor1 = new ColorProperty("fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME);
    protected final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    protected final ColorProperty FontColorTimes = new ColorProperty("FontColorTimes", PrunnWidgetSet_wtcc_2011.FONT_BLUE_TIMES);
    protected final BooleanProperty showwinner = new BooleanProperty("Show Winner", "showwinner", true);
    protected final BooleanProperty showfastest = new BooleanProperty("Show Fastest Lap", "showfastest", true);
    protected final BooleanProperty showpitstop = new BooleanProperty("Show Pitstop", "showpitstop", true);
    protected final BooleanProperty showinfo = new BooleanProperty("Show Info", "showinfo", true);
    
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    private final FloatValue sessionTime = new FloatValue(-1F, 0.1F);
    //private float timestamp = -1;
    //private float endtimestamp = -1;
    private float pitInTime = -1;
    //private float pittime = -1;
    private float pitLaneTime = -1;
    private BoolValue isInPit = new BoolValue(false);
    private final DelayProperty visibleTime;
    private long visibleEnd;
    private long visibleEndPitStop;
    private IntValue cveh = new IntValue();
    private IntValue speed = new IntValue();
    private long visibleEndW;
    private long visibleEndF;
    private final FloatValue racetime = new FloatValue( -1f, 0.1f );
    private float sessionstart = 0;
    private BoolValue racefinished = new BoolValue();
    
    private int widgetpart = 0;//0-info 1-pitstop 2-fastestlap 3-winner
    private final FloatValue FastestLapTime = new FloatValue(-1F, 0.001F);
    StandardTLCGenerator gen = new StandardTLCGenerator();
    private int NumOfPNG = 0;
    private String[] listPNG;
    
    
    
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
        //int fhPos = TextureImage2D.getStringHeight( "0%C", posFont );
        int rowHeight = height / 2;
        
        imgName.updateSize( width*70/100, height, isEditorMode );
        imgTeam.updateSize( width*72/100, rowHeight, isEditorMode );
        imgTitleW.updateSize( width, rowHeight, isEditorMode );
        imgPit.updateSize( width*70/100, height, isEditorMode );
        
        //int top1 = ( rowHeight - fh ) / 2 + fontyoffset.getValue();
        int top2 = ( rowHeight - fh ) / 2 + rowHeight + fontyoffset.getValue();
        
        dsPos = drawnStringFactory.newDrawnString( "dsPos", width*15/200, top2, Alignment.CENTER, false, wtcc_2011_Race_Numbers_Font.getFont(), isFontAntiAliased(), fontColor1.getColor(), null, "" );
        dsName = drawnStringFactory.newDrawnString( "dsName", width*9/100, top2, Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsName2 = drawnStringFactory.newDrawnString( "dsName2", width*16/100, top2, Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsTeam = drawnStringFactory.newDrawnString( "dsTeam", width*52/100, top2, Alignment.LEFT, false, wtcc_2011_Team_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsTeamW = drawnStringFactory.newDrawnString( "dsTeamW", width*54/100, top2, Alignment.LEFT, false, wtcc_2011_Team_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsTitle = drawnStringFactory.newDrawnString( "dsTitle", width*63/100, top2, Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), FontColorTimes.getColor(), null, "" );
        dsTimeC = drawnStringFactory.newDrawnString( "dsTimeC", width*61/100, top2, Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), FontColorTimes.getColor(), null, "" );

        //Scan Manufacturer Folder
        
        File dir = new File(gameData.getFileSystem().getImagesFolder().toString() + "/prunn/WTCC/Manufacturer");

        String[] children = dir.list();
        NumOfPNG = 0;
        listPNG = new String[children.length];
        
        for (int i=0; i < children.length; i++) 
        {
            // Get filename of file or directory
            String filename = children[i];
            
            if(filename.substring( filename.length()-4 ).toUpperCase().equals( ".PNG" ) )
            {
                //log(filename.substring( 0, filename.length()-4 ));
                listPNG[NumOfPNG] = filename.substring( 0, filename.length()-4 );
                NumOfPNG++;
            }    
        }
        
        

        //end of scan
    }
    protected Boolean updateVisibility(LiveGameData gameData, boolean isEditorMode)
    {
        
        
        super.updateVisibility(gameData, isEditorMode);
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        cveh.update(gameData.getScoringInfo().getViewedVehicleScoringInfo().getDriverId());
        isInPit.update(scoringInfo.getViewedVehicleScoringInfo().isInPits());
        //fastest lap
        Laptime lt = scoringInfo.getFastestLaptime();
        
        if(lt == null || !lt.isFinished())
            FastestLapTime.update(-1F);
        else
            FastestLapTime.update(lt.getLapTime());
        //winner part
        if(gameData.getScoringInfo().getLeadersVehicleScoringInfo().getLapsCompleted() < 1)
            sessionstart = gameData.getScoringInfo().getLeadersVehicleScoringInfo().getLapStartTime();
        if(scoringInfo.getSessionTime() > 0)
            racetime.update( scoringInfo.getSessionTime() - sessionstart );
        
        racefinished.update(gameData.getScoringInfo().getViewedVehicleScoringInfo().getFinishStatus().isFinished());
        
               
        //carinfo
        if(cveh.hasChanged() && cveh.isValid() && showinfo.getValue() && !isEditorMode)
        {
            forceCompleteRedraw(true);
            visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            isvisible = true;
            widgetpart = 0;
            return true;
        }
        
        if(scoringInfo.getSessionNanos() < visibleEnd )
        {
            forceCompleteRedraw(true);
            isvisible = true;
            widgetpart = 0;
            return true;
        }
        
        //pitstop   
        if( isInPit.hasChanged())
        {
            if(isInPit.getValue())
            {
                pitLaneTime = 0;
                //pittime = 0;
                pitInTime = gameData.getScoringInfo().getSessionTime();
                forceCompleteRedraw(true);
            }
            
            
            //endtimestamp = 0;
            //timestamp = 0;
            
        }
            
        if( isInPit.getValue() && showpitstop.getValue() )
        {
            if(scoringInfo.getViewedVehicleScoringInfo().getLapsCompleted() > 0)
                widgetpart = 1;
            else
            {
                widgetpart = 0;
            }
            
            speed.update( (int)scoringInfo.getViewedVehicleScoringInfo().getScalarVelocity());
            /*if(speed.hasChanged() && speed.getValue() < 2)
            {//ai gets to 5-6 kmh when they drop
                endtimestamp = gameData.getScoringInfo().getSessionTime();
                timestamp = gameData.getScoringInfo().getSessionTime();
            }
            else
                if(speed.getValue() < 2)
                    endtimestamp = gameData.getScoringInfo().getSessionTime();*/
                
            
            visibleEndPitStop = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            isvisible = true;
            return true;
        }
        
        if(scoringInfo.getSessionNanos() < visibleEndPitStop )
        {
            forceCompleteRedraw(true);
            isvisible = true;
            widgetpart = 1;
            return true;
        }
        
        //fastest lap
        if(scoringInfo.getSessionNanos() < visibleEndF && FastestLapTime.isValid())
        {
            isvisible = true;
            widgetpart = 2;
            return true; 
        }
        if(FastestLapTime.hasChanged() && FastestLapTime.isValid() && scoringInfo.getLeadersVehicleScoringInfo().getLapsCompleted() > 1 && showfastest.getValue())
        {
            forceCompleteRedraw(true);
            visibleEndF = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            isvisible = true;
            widgetpart = 2;
            return true;
        }
        
        //winner part
        if(scoringInfo.getSessionNanos() < visibleEndW )
        {
            isvisible = true;
            widgetpart = 3;
            return true;
        }
         
        if(racefinished.hasChanged() && racefinished.getValue() && showwinner.getValue() )
        {
            forceCompleteRedraw(true);
            visibleEndW = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos()*2;
            isvisible = true;
            widgetpart = 3;
            return true;
        }
        isvisible = false;
        return false;	
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        String team;
        VehicleScoringInfo vsi1 = gameData.getScoringInfo().getViewedVehicleScoringInfo();
                                
        int rowHeight = height / 2;
        if(isEditorMode)
            widgetpart = 4;
        switch(widgetpart)
        {
            case 1: //Pit Stop
                    texture.clear( imgPit.getTexture(), offsetX, offsetY, false, null );
                    if(isEditorMode)
                        team = "seat";
                    else if(vsi1.getVehicleInfo() != null)
                        team = vsi1.getVehicleInfo().getManufacturer();
                    else
                        team = vsi1.getVehicleClass();

                    for(int i=0; i < NumOfPNG; i++)
                    {
                        if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                        {
                            //log(listPNG[i]);
                            imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                            texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                            break;
                        }
                    }
                    
                    /*if(team.length() >= 3 && team.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
                    {
                        texManufacturer = imgBMW.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }
                    else if((team.length() >= 4 && team.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) || isEditorMode) 
                    {
                        texManufacturer = imgSeat.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }
                    else if(team.length() >= 5 && team.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
                    {
                        texManufacturer = imgVolvo.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }
                    else if(team.length() >= 9 && team.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
                    {
                        texManufacturer = imgChevy.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }*/
                    break;
        
            case 2: //Fastest Lap
                    texture.clear( imgName.getTexture(), offsetX, offsetY, false, null );
                    VehicleScoringInfo fastcarinfos = gameData.getScoringInfo().getFastestLapVSI();
                    
                    
                    if(isEditorMode)
                        team = "BMW";
                    else if(fastcarinfos.getVehicleInfo() != null)
                        team = fastcarinfos.getVehicleInfo().getManufacturer();
                    else
                        team = fastcarinfos.getVehicleClass();

                    for(int i=0; i < NumOfPNG; i++)
                    {
                        if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                        {
                            imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                            texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                            break;
                        }
                    }
                    
                    /*if(team.length() >= 3 && team.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
                    {
                        texManufacturer = imgBMW.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }
                    else if((team.length() >= 4 && team.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) || isEditorMode) 
                    {
                        texManufacturer = imgSeat.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }
                    else if(team.length() >= 5 && team.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
                    {
                        texManufacturer = imgVolvo.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }
                    else if(team.length() >= 9 && team.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
                    {
                        texManufacturer = imgChevy.getImage().getScaledTextureImage( width*6/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*34/100, offsetY + height*60/100, true, null );
                    }*/
                    break;
                    
            case 3: //Winner
                    texture.clear( imgTitleW.getTexture(), offsetX, offsetY + rowHeight, false, null );
                    VehicleScoringInfo winnercarinfos = gameData.getScoringInfo().getLeadersVehicleScoringInfo();
                    
                    if(isEditorMode)
                        team = "chevrolet";
                    else if(winnercarinfos.getVehicleInfo() != null)
                        team = winnercarinfos.getVehicleInfo().getManufacturer();
                    else
                        team = winnercarinfos.getVehicleClass();

                    for(int i=0; i < NumOfPNG; i++)
                    {
                        if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                        {
                            //log(listPNG[i]);
                            imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                            texture.drawImage( texManufacturer, offsetX + width*52/100, offsetY + height*60/100, true, null );
                            break;
                        }
                    }
                    
                    /*if(team.length() >= 3 && team.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
                    {
                        texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*52/100, offsetY + height*60/100, true, null );
                    }
                    else if((team.length() >= 4 && team.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) || isEditorMode) 
                    {
                        texManufacturer = imgSeat.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*52/100, offsetY + height*60/100, true, null );
                    }
                    else if(team.length() >= 5 && team.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
                    {
                        texManufacturer = imgVolvo.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*52/100, offsetY + height*60/100, true, null );
                    }
                    else if(team.length() >= 9 && team.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
                    {
                        texManufacturer = imgChevy.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                        texture.drawImage( texManufacturer, offsetX + width*52/100, offsetY + height*60/100, true, null );
                    }*/
                    break;
            
            default: //Info
                    texture.clear( imgTeam.getTexture(), offsetX, offsetY + rowHeight, false, null );
                    
                    
                    if(isEditorMode)
                        team = "BMW";
                    else if(vsi1.getVehicleInfo() != null)
                        team = vsi1.getVehicleInfo().getManufacturer();
                    else
                        team = vsi1.getVehicleClass();

                    for(int i=0; i < NumOfPNG; i++)
                    {
                        if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                        {
                            //log(listPNG[i]);
                            imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                            texture.drawImage( texManufacturer, offsetX + width*50/100, offsetY + height*60/100, true, null );
                            break;
                        }
                    }
                    
                    
                    
                    break;
        }
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
    	sessionTime.update(scoringInfo.getSessionTime());
    	if(isEditorMode)
            widgetpart = 4;
    	
    	if ( needsCompleteRedraw || sessionTime.hasChanged() || FastestLapTime.hasChanged())
        {
    	    String pos = "";
    	    String top1info1="";
    	    //String top1info2="";
    	    String top2info1;
    	    //String top2info2="";
    	    //String top2info2b="";
    	    String top2info2c="";
            String top3info1;
    	    String top3info2="";
    	    //String top3info2b="";
    	    //String top3info2c="";
            
    	    switch(widgetpart)
            {
                case 1: //Pit Stop
                        VehicleScoringInfo currentcarinfos = gameData.getScoringInfo().getViewedVehicleScoringInfo();
                        /*if(currentcarinfos.getNumOutstandingPenalties() > 0)
                            top3info2="";
                        else
                            top3info2="";*/
                        
                        if(isInPit.getValue())
                        {
                            pitLaneTime = gameData.getScoringInfo().getSessionTime() - pitInTime;
                            top2info2c = TimingUtil.getTimeAsString(pitLaneTime, false, false, true, false ) + "    ";
                        }
                        else
                            top2info2c = TimingUtil.getTimeAsString(pitLaneTime, false, false, true, true );
                            
                        //if(scoringInfo.getViewedVehicleScoringInfo().getScalarVelocity() < 2)
                            //pittime = endtimestamp - timestamp;
                        
                        top1info1 = gen.ShortNameWTCC( currentcarinfos.getDriverName().toUpperCase());
                        top2info1 = "Pit Lane";
                        //top3info2c = TimingUtil.getTimeAsString(pittime, false, false, true, false );
                        top3info1 = "Pit Stop";
                        
                        dsName.draw( offsetX, offsetY, top1info1, texture );
                        //dsTeam.draw( offsetX, offsetY, top3info1, texture );
                        //dsWinner.draw( offsetX, offsetY, top2info1, texture );
                        dsTimeC.draw( offsetX, offsetY, top2info2c, texture);
                        //dsTitleC.draw( offsetX, offsetY, top3info2c, texture );
                        
                        break;
                    
                case 2: //Fastest Lap
                        VehicleScoringInfo fastcarinfos = gameData.getScoringInfo().getFastestLapVSI();
                        
                        if(fastcarinfos.getVehicleInfo() != null)
                            top3info1 = fastcarinfos.getVehicleInfo().getManufacturer();
                        else
                            top3info1 = fastcarinfos.getVehicleClass(); 
                        
                        top2info1 = gen.ShortNameWTCC( fastcarinfos.getDriverName().toUpperCase());
                        
                        top3info2 = TimingUtil.getTimeAsLaptimeString(FastestLapTime.getValue() );
                        
                        dsName.draw( offsetX, offsetY, top2info1, texture );
                        dsTitle.draw( offsetX, offsetY, top3info2, texture );
                        
                        break;
                        
                case 3: //Winner
                        VehicleScoringInfo winnercarinfos = gameData.getScoringInfo().getLeadersVehicleScoringInfo();
                        
                        if(isEditorMode)
                            top3info1 = "Chevrolet Cruze";
                        else if(winnercarinfos.getVehicleInfo() != null)
                            top3info1 = winnercarinfos.getVehicleInfo().getManufacturer();
                        else
                            top3info1 = winnercarinfos.getVehicleClass(); 
                        
                        
                        float laps=0;
                        
                        for(int i=1;i <= winnercarinfos.getLapsCompleted(); i++)
                        {
                            if(winnercarinfos.getLaptime(i) != null)
                                laps = winnercarinfos.getLaptime(i).getLapTime() + laps;
                            else
                            {
                                laps = racetime.getValue();
                                i = winnercarinfos.getLapsCompleted()+1;
                            }
                        } 
                        
                        top2info1 = gen.ShortNameWTCC( winnercarinfos.getDriverName().toUpperCase());
                        
                        for(int i=0; i < NumOfPNG; i++)
                        {
                            if(top3info1.length() >= listPNG[i].length() && top3info1.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                            {
                                if(top3info1.length() == listPNG[i].length())
                                    top3info1 = "      " + top3info1;
                                else
                                    top3info1 = "      " + top3info1.substring( listPNG[i].length() );
                                break;
                            }
                        }
                        
                        
                        
                        dsName.draw( offsetX, offsetY, top2info1, texture );
                        dsTeamW.draw( offsetX, offsetY, top3info1, texture );
                        
                        break;
                
                default: //Info
                        VehicleScoringInfo currentcarinfosInfo = gameData.getScoringInfo().getViewedVehicleScoringInfo();
                        if(isEditorMode)
                            top3info1 = "BMW 320 TC";
                        else if(currentcarinfosInfo.getVehicleInfo() != null)
                            top3info1 = currentcarinfosInfo.getVehicleInfo().getManufacturer();
                        else
                            top3info1 = currentcarinfosInfo.getVehicleClass(); 
                        
                        for(int i=0; i < NumOfPNG; i++)
                        {
                            if(top3info1.length() >= listPNG[i].length() && top3info1.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                            {
                                if(top3info1.length() == listPNG[i].length())
                                    top3info1 = "      " + top3info1;
                                else
                                    top3info1 = "      " + top3info1.substring( listPNG[i].length() );
                                break;
                            }
                        }
                        
                        /*if(top3info1.length() >= 3 && top3info1.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
                            top3info1 = "     " + top3info1.substring( 3 );
                        else if(top3info1.length() >= 4 && top3info1.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) 
                            top3info1 = "     " + top3info1.substring( 4 );
                        else if(top3info1.length() >= 5 && top3info1.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
                            top3info1 = "     " + top3info1.substring( 5 );
                        else if(top3info1.length() >= 9 && top3info1.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
                            top3info1 = "     " + top3info1.substring( 9 );
                        else if(top3info1.length() > 10)
                            top3info1 = top3info1.substring( 0, 10 );*/
                        
                        
                        
                        top2info1 = gen.ShortNameWTCC( currentcarinfosInfo.getDriverName().toUpperCase());
                        pos = Integer.toString( currentcarinfosInfo.getPlace(false) );
                        
                        dsPos.draw( offsetX, offsetY, pos, texture );
                        dsName2.draw( offsetX, offsetY, top2info1, texture );
                        dsTeam.draw( offsetX, offsetY, top3info1, texture );
                        
                        break;
            }
    	    
        	
        }
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( wtcc_2011_Times_Font, "" );
        writer.writeProperty( wtcc_2011_Team_Font, "" );
        writer.writeProperty( wtcc_2011_Race_Numbers_Font, "" );
        writer.writeProperty( posFont, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( FontColorTimes, "" );
        writer.writeProperty(visibleTime, "");
        writer.writeProperty(showwinner, "");
        writer.writeProperty(showfastest, "");
        writer.writeProperty(showpitstop, "");
        writer.writeProperty(showinfo, "");
        writer.writeProperty( fontyoffset, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( wtcc_2011_Times_Font ) );
        else if ( loader.loadProperty( wtcc_2011_Team_Font ) );
        else if ( loader.loadProperty( wtcc_2011_Race_Numbers_Font ) );
        else if ( loader.loadProperty( posFont ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( FontColorTimes ) );
        else if( loader.loadProperty(visibleTime));
        else if ( loader.loadProperty( showwinner ) );
        else if ( loader.loadProperty( showfastest ) );
        else if ( loader.loadProperty( showpitstop ) );
        else if ( loader.loadProperty( showinfo ) );
        else if ( loader.loadProperty( fontyoffset ) );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Colors" );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( wtcc_2011_Times_Font );
        propsCont.addProperty( wtcc_2011_Team_Font );
        propsCont.addProperty( wtcc_2011_Race_Numbers_Font );
        propsCont.addProperty( posFont );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty( FontColorTimes );
        propsCont.addProperty(visibleTime);
        propsCont.addProperty(showwinner);
        propsCont.addProperty(showfastest);
        propsCont.addProperty(showpitstop);
        propsCont.addProperty(showinfo);
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
        
        //getFontProperty().setFont( "Dialog", Font.PLAIN, 6, false, true );
        
    }
    
    public RaceInfosWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011_Race, 36.0f, 11.6f );
        visibleTime = new DelayProperty("visibleTime", net.ctdp.rfdynhud.properties.DelayProperty.DisplayUnits.SECONDS, 6);
        visibleEnd = 0;
        getBackgroundProperty().setColorValue( "#00000000" );
        //getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
    }
    
}
