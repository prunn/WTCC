package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.resultmonitor;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
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


public class ResultMonitorWidget extends Widget
{
    private DrawnString[] dsPos = null;
    private DrawnString[] dsName = null;
    private DrawnString[] dsTeam = null;
    private DrawnString[] dsTime = null;
    private DrawnString dsSession = null;
    
    private final ImagePropertyWithTexture imgSession = new ImagePropertyWithTexture( "imgSession", "prunn/WTCC/monitor_title.png" );
    private final ImagePropertyWithTexture imgPos = new ImagePropertyWithTexture( "imgPos", "prunn/WTCC/monitor.png" );
    protected final FontProperty wtcc_2011_Race_Numbers_Font = new FontProperty("Pos Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_RACE_NUMBERS);
    
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    protected final FontProperty wtcc_2011_Times_Font = new FontProperty("Time Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TIMES);
    private final ColorProperty fontColor1 = new ColorProperty( "fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    private final ColorProperty fontColor2 = new ColorProperty( "fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME );
    
    private final IntProperty numVeh = new IntProperty( "numberOfVehicles", 10 );
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    private IntProperty fontxposoffset = new IntProperty("X Position Font Offset", 0);
    private IntProperty fontxnameoffset = new IntProperty("X Name Font Offset", 0);
    private IntProperty fontxtimeoffset = new IntProperty("X Time Font Offset", 0);
    private TextureImage2D texManufacturer = null;
    private final ImagePropertyWithTexture imgBMW = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/bmw.png" );
    
    private IntValue[] positions = null;
    private StringValue[] driverNames = null;
    private StringValue[] driverTeam = null;
    private FloatValue[] gaps = null;
    private BooleanProperty AbsTimes = new BooleanProperty("Use absolute times", false) ;
    private int NumOfPNG = 0;
    private String[] listPNG;
    
    
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
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
    
    private void initValues()
    {
        int maxNumItems = numVeh.getValue();
        
        if ( ( positions != null ) && ( positions.length == maxNumItems ) )
            return;
        
        gaps = new FloatValue[maxNumItems];
        positions = new IntValue[maxNumItems];
        driverNames = new StringValue[maxNumItems];
        driverTeam = new StringValue[maxNumItems];
        
        for(int i=0;i < maxNumItems;i++)
        { 
            positions[i] = new IntValue();
            driverNames[i] = new StringValue();
            driverTeam[i] = new StringValue();
            gaps[i] = new FloatValue();
        }
        
        
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int maxNumItems = numVeh.getValue();
        int fh = TextureImage2D.getStringHeight( "0%C", getFontProperty() );
        int rowHeight = height / (maxNumItems + 3);
        
        //imgTrack.updateSize( width*80/100, rowHeight, isEditorMode );
        imgSession.updateSize( width*90/100, rowHeight*12/10, isEditorMode );
        //imgFirst.updateSize( width, rowHeight, isEditorMode );
        imgPos.updateSize( width, rowHeight, isEditorMode );
        //imgTeam.updateSize( width*33/100, rowHeight, isEditorMode );
        //imgTime.updateSize( width*23/100, rowHeight, isEditorMode );
        Color whiteFontColor = fontColor2.getColor();
        
        dsPos = new DrawnString[maxNumItems];
        dsName = new DrawnString[maxNumItems];
        dsTeam = new DrawnString[maxNumItems];
        dsTime = new DrawnString[maxNumItems];
        
        
        int top = ( rowHeight - fh ) / 2;
        
        //dsTrack = drawnStringFactory.newDrawnString( "dsTrack", width*5/100, top, Alignment.LEFT, false, f1_2011Font.getFont(), isFontAntiAliased(), fontColor1.getColor() );
        top += rowHeight;
        dsSession = drawnStringFactory.newDrawnString( "dsSession", width*19/100, top - 5 + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor() );
        top += rowHeight;
        top += rowHeight;
        
        for(int i=0;i < maxNumItems;i++)
        {
            dsPos[i] = drawnStringFactory.newDrawnString( "dsPos", width*13/200 + fontxposoffset.getValue(), top + fontyoffset.getValue(), Alignment.CENTER, false, wtcc_2011_Race_Numbers_Font.getFont(), isFontAntiAliased(), fontColor1.getColor() );
            dsName[i] = drawnStringFactory.newDrawnString( "dsName", width*14/100 + fontxnameoffset.getValue(), top + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), whiteFontColor );
            dsTeam[i] = drawnStringFactory.newDrawnString( "dsTeam", width*57/100 + fontxnameoffset.getValue(), top + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), whiteFontColor );
            dsTime[i] = drawnStringFactory.newDrawnString( "dsTime",  width*97/100 + fontxtimeoffset.getValue(), top + fontyoffset.getValue(), Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), whiteFontColor );
            
            top += rowHeight;
        }
        
        
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
    
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        super.updateVisibility( gameData, isEditorMode );
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        int drawncars = Math.min( scoringInfo.getNumVehicles(), numVeh.getValue() );
        initValues();
        boolean RedrawFlag = false;
        
        for(int i=0;i < drawncars;i++)
        { 
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            if(vsi != null)
            {
                positions[i].update( vsi.getPlace( false ) );
                driverNames[i].update( PrunnWidgetSet_wtcc_2011.ShortNameWTCC( vsi.getDriverName().toUpperCase()) );
                //driverTeam[i].update( gen.generateShortTeamNames( vsi.getVehicleInfo().getFullTeamName(), gameData.getFileSystem().getConfigFolder() ));
                
                if(vsi.getVehicleInfo() != null)
                    driverTeam[i].update( vsi.getVehicleInfo().getManufacturer() );
                else
                    driverTeam[i].update( vsi.getVehicleClass());
               
                if(scoringInfo.getSessionType() != SessionType.RACE1)
                    gaps[i].update(vsi.getBestLapTime());
                else
                    gaps[i].update(vsi.getNumPitstopsMade());
                    
                 if(isEditorMode)
                 {
                     int randomTeam = (int)( Math.random()*4 );
                     switch(randomTeam)
                     {
                         case 0:
                             driverTeam[i].update( "Seat Leon 1.6" );
                             break;
                         case 1:
                             driverTeam[i].update( "BMW 320 TC" );
                             break;
                         case 2:
                             driverTeam[i].update( "Volvo C30" );
                             break;
                         default:
                             driverTeam[i].update( "Chevrolet Cruze" );
                             break;
                     }
                     
                 }
                 if(driverTeam[i].hasChanged())
                     RedrawFlag = true;
            }
        }
        if(!isEditorMode && RedrawFlag)
            forceCompleteRedraw(true);
        return true;
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        int maxNumItems = numVeh.getValue();
        int drawncars = Math.min( scoringInfo.getNumVehicles(), maxNumItems );
        int rowHeight = height / (maxNumItems + 3);
        
        texture.clear( imgSession.getTexture(), offsetX, offsetY+rowHeight*80/100, false, null );
        
        for(int i=0;i < drawncars;i++)
        {
            texture.clear( imgPos.getTexture(), offsetX, offsetY+rowHeight*(i+3), false, null );
        

            for(int j=0; j < NumOfPNG; j++)
            {
                if(driverTeam[i].getValue().length() >= listPNG[j].length() && driverTeam[i].getValue().substring( 0, listPNG[j].length() ).toUpperCase().equals( listPNG[j].toUpperCase() )) 
                {
                    imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[j] + ".png");
                    texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, rowHeight*86/100, texManufacturer, isEditorMode );
                    texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + rowHeight*(i+3) + rowHeight*10/100, true, null );
                    break;
                }
            }
            
            
        }
        
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        int drawncars = Math.min( scoringInfo.getNumVehicles(), numVeh.getValue() );
        String SessionName;
        //one time for leader
        
        if ( needsCompleteRedraw || clock.c())
        {
            switch(scoringInfo.getSessionType())
            {
                case RACE1: case RACE2: case RACE3: case RACE4:
                    SessionName = "Race";
                    break;
                case QUALIFYING1: case QUALIFYING2: case QUALIFYING3: case QUALIFYING4: 
                    SessionName = "Qualifying";
                    break;
                case PRACTICE1:
                    SessionName = "Practice 1";
                    break;
                case PRACTICE2:
                    SessionName = "Practice 2";
                    break;
                case PRACTICE3:
                    SessionName = "Practice 3";
                    break;
                case PRACTICE4:
                    SessionName = "Practice 4";
                    break;
                case TEST_DAY:
                    SessionName = "Test";
                    break;
                case WARMUP:
                    SessionName = "Warmup";
                    break;
                default:
                    SessionName = "";
                    break;
                        
            }
            
            for(int j=0; j < NumOfPNG; j++)
            {
                if(driverTeam[0].getValue().length() >= listPNG[j].length() && driverTeam[0].getValue().substring( 0, listPNG[j].length() ).toUpperCase().equals( listPNG[j].toUpperCase() )) 
                {
                    if(driverTeam[0].getValue().length() == listPNG[j].length())
                        driverTeam[0].update( "      " + driverTeam[0].getValue());
                    else
                        driverTeam[0].update( "      " + driverTeam[0].getValue().substring( listPNG[j].length() ));
                    break;
                }
            }
            
            
            /*if(driverTeam[0].getValue().length() >= 3 && driverTeam[0].getValue().substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
                driverTeam[0].update( "     " + driverTeam[0].getValue().substring( 3 ) );
            else if(driverTeam[0].getValue().length() >= 4 && driverTeam[0].getValue().substring( 0, 4 ).toUpperCase().equals( "SEAT" )) 
                driverTeam[0].update( "     " + driverTeam[0].getValue().substring( 4 ) );
            else if(driverTeam[0].getValue().length() >= 5 && driverTeam[0].getValue().substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
                driverTeam[0].update( "     " + driverTeam[0].getValue().substring( 5 ) );
            else if(driverTeam[0].getValue().length() >= 9 && driverTeam[0].getValue().substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
                driverTeam[0].update( "     " + driverTeam[0].getValue().substring( 9 ) );
            else*/ 
            if(driverTeam[0].getValue().length() > 15)
                driverTeam[0].update( driverTeam[0].getValue().substring( 0, 15 ) );
            
            //dsTrack.draw( offsetX, offsetY, gameData.getTrackInfo().getTrackName(), texture);
            dsSession.draw( offsetX, offsetY, gameData.getTrackInfo().getTrackName() + " - " + SessionName, texture);
            
            dsPos[0].draw( offsetX, offsetY, positions[0].getValueAsString(), texture );
            dsName[0].draw( offsetX, offsetY, driverNames[0].getValue(), texture );
            dsTeam[0].draw( offsetX, offsetY, driverTeam[0].getValue(), texture );
            
            if(scoringInfo.getSessionType() == SessionType.RACE1 )
            {
                String stops = ( scoringInfo.getLeadersVehicleScoringInfo().getNumPitstopsMade() > 1 ) ? " Stops" : " Stop";
                dsTime[0].draw( offsetX, offsetY, scoringInfo.getLeadersVehicleScoringInfo().getNumPitstopsMade() + stops, texture);
            }
            else
                if(gaps[0].isValid())
                    dsTime[0].draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString(gaps[0].getValue() ) , texture);
                else
                    dsTime[0].draw( offsetX, offsetY, "No Time" , texture);
        
        
            // the other guys
            for(int i=1;i < drawncars;i++)
            { 
                if ( needsCompleteRedraw || clock.c() )
                {
                    //log(driverTeam[i].getValue());
                    for(int j=0; j < NumOfPNG; j++)
                    {
                        if(driverTeam[i].getValue().length() >= listPNG[j].length() && driverTeam[i].getValue().substring( 0, listPNG[j].length() ).toUpperCase().equals( listPNG[j].toUpperCase() )) 
                        {
                            if(driverTeam[i].getValue().length() == listPNG[j].length())
                                driverTeam[i].update( "      " + driverTeam[i].getValue());
                            else
                                driverTeam[i].update( "      " + driverTeam[i].getValue().substring( listPNG[j].length() ));
                            break;
                        }
                    }
                    /*if(driverTeam[i].getValue().length() >= 3 && driverTeam[i].getValue().substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
                        driverTeam[i].update( "     " + driverTeam[i].getValue().substring( 3 ) );
                    else if(driverTeam[i].getValue().length() >= 4 && driverTeam[i].getValue().substring( 0, 4 ).toUpperCase().equals( "SEAT" )) 
                        driverTeam[i].update( "     " + driverTeam[i].getValue().substring( 4 ) );
                    else if(driverTeam[i].getValue().length() >= 5 && driverTeam[i].getValue().substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
                        driverTeam[i].update( "     " + driverTeam[i].getValue().substring( 5 ) );
                    else if(driverTeam[i].getValue().length() >= 9 && driverTeam[i].getValue().substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
                        driverTeam[i].update( "     " + driverTeam[i].getValue().substring( 9 ) );
                    else*/ 
                    if(driverTeam[i].getValue().length() > 15)
                        driverTeam[i].update( driverTeam[i].getValue().substring( 0, 15 ) );
                    
                    dsPos[i].draw( offsetX, offsetY, positions[i].getValueAsString(), texture );
                    dsName[i].draw( offsetX, offsetY,driverNames[i].getValue() , texture );  
                    dsTeam[i].draw( offsetX, offsetY, driverTeam[i].getValue(), texture );
                    if(scoringInfo.getVehicleScoringInfo( i ).getFinishStatus() == FinishStatus.DQ)
                        dsTime[i].draw( offsetX, offsetY, "DQ", texture);
                    else
                        if(scoringInfo.getSessionType() == SessionType.RACE1 )
                        {
                            if(scoringInfo.getVehicleScoringInfo( i ).getFinishStatus() == FinishStatus.DNF)
                                dsTime[i].draw( offsetX, offsetY, "DNF", texture); 
                            else
                            {
                                String stops = ( scoringInfo.getVehicleScoringInfo( i ).getNumPitstopsMade() > 1 ) ? " Stops" : " Stop";
                                dsTime[i].draw( offsetX, offsetY, scoringInfo.getVehicleScoringInfo( i ).getNumPitstopsMade() + stops, texture);
                            }
                        }
                        else
                            if(!gaps[i].isValid())
                                dsTime[i].draw( offsetX, offsetY, "No Time", texture);
                            else
                                if(AbsTimes.getValue() || !gaps[0].isValid())
                                   dsTime[i].draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString(gaps[i].getValue() ), texture);
                                else
                                    dsTime[i].draw( offsetX, offsetY,"+ " + TimingUtil.getTimeAsLaptimeString(Math.abs( gaps[i].getValue() - gaps[0].getValue() )) , texture);
                    //log(driverTeam[i].getValue());
                  }
                
            }
        }
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( wtcc_2011_Times_Font, "" );
        writer.writeProperty( wtcc_2011_Race_Numbers_Font, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( numVeh, "" );
        writer.writeProperty( AbsTimes, "" );
        writer.writeProperty( fontyoffset, "" );
        writer.writeProperty( fontxposoffset, "" );
        writer.writeProperty( fontxnameoffset, "" );
        writer.writeProperty( fontxtimeoffset, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( wtcc_2011_Times_Font ) );
        else if ( loader.loadProperty( wtcc_2011_Race_Numbers_Font ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( numVeh ) );
        else if ( loader.loadProperty( AbsTimes ) );
        else if ( loader.loadProperty( fontyoffset ) );
        else if ( loader.loadProperty( fontxposoffset ) );
        else if ( loader.loadProperty( fontxnameoffset ) );
        else if ( loader.loadProperty( fontxtimeoffset ) );
    }
    
    @Override
    protected void addFontPropertiesToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Colors and Fonts" );
        
        super.addFontPropertiesToContainer( propsCont, forceAll );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( wtcc_2011_Times_Font );
        propsCont.addProperty( wtcc_2011_Race_Numbers_Font );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( numVeh );
        propsCont.addProperty( AbsTimes );
        propsCont.addGroup( "Font Displacement" );
        propsCont.addProperty( fontyoffset );
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
    
    public ResultMonitorWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 66.4f, 46.5f );
        
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
        getFontColorProperty().setColor( PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    }
}
