package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.racegap;

import java.awt.Font;
import java.io.File;
import java.io.IOException;

import com.prunn.rfdynhud.plugins.tlcgenerator.StandardTLCGenerator;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;
import com.prunn.rfdynhud.widgets.prunn.wtcc_2011.raceinfos.RaceInfosWidget;

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
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class RaceGapWidget extends Widget
{
    private DrawnString dsPos = null;
    private DrawnString dsPos2 = null;
    private DrawnString dsName = null;
    private DrawnString dsName2 = null;
    private DrawnString dsTeam = null;
    private DrawnString dsTeam2 = null;
    private DrawnString dsTime = null;
    private TextureImage2D texManufacturer = null;
    private TextureImage2D texTime = null;
    private final ImagePropertyWithTexture imgPos = new ImagePropertyWithTexture( "imgPos", "prunn/WTCC/info.png" );
    private final ImagePropertyWithTexture imgTime = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/lap_slower.png" );
    private final ImagePropertyWithTexture imgBMW = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/bmw.png" );
    
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    protected final IntProperty frequency = new IntProperty("appearence frequency", "frequency", 3);
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    protected final FontProperty wtcc_2011_Team_Font = new FontProperty("Team Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TEAMS);
    protected final FontProperty wtcc_2011_Race_Numbers_Font = new FontProperty("Team Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_RACE_NUMBERS);
    protected final FontProperty wtcc_2011_Times_Font = new FontProperty("Time Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TIMES);
    protected final ColorProperty fontColor1 = new ColorProperty("fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME);
    protected final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    protected final ColorProperty FontColorTimes = new ColorProperty("FontColorTimes", PrunnWidgetSet_wtcc_2011.FONT_BLUE_TIMES);
    private int NumOfPNG = 0;
    private String[] listPNG;
    
    private final DelayProperty visibleTime;
    private long visibleEnd;
    private final IntValue CurrentSector = new IntValue();
    private String  name, name2, team, team2;
    private int place, place2;
    private String gap;
    StandardTLCGenerator gen = new StandardTLCGenerator();
    public static Boolean isvisible = false;
    public static Boolean visible()
    {
        return isvisible;
    }
    
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        String cpid = "Y29weXJpZ2h0QFBydW5uMjAxMQ";
        if(!isEditorMode)
            log(cpid);
    }
    private static final String getTimeAsGapString2( float gap )
    {
        return ( "+ " + TimingUtil.getTimeAsLaptimeString( gap ) );
    }
    private void fillvsis(LiveGameData gameData)
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo viewedvsi = scoringInfo.getViewedVehicleScoringInfo();
        VehicleScoringInfo vsi1;
        VehicleScoringInfo vsi2;
        float GapFront = 1000.000f;
        float GapBehind = 0.000f;
        int LapFront = 1000;
        int LapBehind = 0;
        
        if(viewedvsi.getNextInFront( false ) != null)
        {
            GapFront = Math.abs(viewedvsi.getTimeBehindNextInFront( false ));
            LapFront = viewedvsi.getLapsBehindNextInFront( false );
        }
        if(viewedvsi.getNextBehind( false ) != null)
        {
            GapBehind = Math.abs( viewedvsi.getNextBehind( false ).getTimeBehindNextInFront( false ));
            LapBehind = viewedvsi.getNextBehind( false ).getLapsBehindNextInFront( false );
        }
        
        if(viewedvsi.getNextBehind( false ) == null || GapFront < GapBehind || LapFront < LapBehind)
        {
            vsi1 = viewedvsi.getNextInFront( false );
            vsi2 = viewedvsi;
            if(LapFront == 0)
                gap = getTimeAsGapString2(GapFront);
            else
            {
                String laps = ( LapFront > 1 ) ? " Laps" : " Lap";
                gap = "+ " + LapFront + laps;
            }
        }
        else
        {
            vsi1 = viewedvsi;
            vsi2 = viewedvsi.getNextBehind( false );
            if(LapBehind == 0)
                gap = getTimeAsGapString2(GapBehind);
            else
            {
                String laps = ( LapBehind > 1 ) ? " Laps" : " Lap";
                gap = "+ " + LapBehind + laps;
            }
        }
        
        place = vsi1.getPlace(false);
        name = gen.ShortNameWTCC(  vsi1.getDriverName());
        place2 = vsi2.getPlace(false);
        name2 = gen.ShortNameWTCC(  vsi2.getDriverName() );
        //team = gen.generateShortTeamNames( vsi1.getVehicleInfo().getFullTeamName(), gameData.getFileSystem().getConfigFolder() );
        //team2 = gen.generateShortTeamNames( vsi2.getVehicleInfo().getFullTeamName(), gameData.getFileSystem().getConfigFolder() );
        
        if(vsi1.getVehicleInfo() != null)
            team = vsi1.getVehicleInfo().getManufacturer();
        else
            team = vsi1.getVehicleClass();
        
        /*if(team.length() > 10)
            team = team.substring( 0, 10 );*/
        
        if(vsi2.getVehicleInfo() != null)
            team2 = vsi2.getVehicleInfo().getManufacturer();
        else
            team2 = vsi2.getVehicleClass();
        
        /*if(team2.length() > 10)
            team2 = team2.substring( 0, 10 );*/
        
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
        
        imgPos.updateSize( width*78/100, height*49/100, isEditorMode );
        imgTime.updateSize( width*22/100, height*49/100, isEditorMode );
        texTime = imgTime.getImage().getScaledTextureImage( width*22/100, height*49/100, texTime, isEditorMode );
        
        int fh = TextureImage2D.getStringHeight( "0yI", wtcc_2011_Font );
        int top1 = ( height - fh ) / 4 + fontyoffset.getValue() - 5;
        int top2 = ( height - fh ) / 4 + height/2 + fontyoffset.getValue() - 5;
        
        dsPos = drawnStringFactory.newDrawnString( "dsPos", width*8/100, top1, Alignment.CENTER, false, wtcc_2011_Race_Numbers_Font.getFont(), isFontAntiAliased(), fontColor1.getColor(), null, "" );
        dsName = drawnStringFactory.newDrawnString( "dsName", width*16/100, top1, Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsTeam = drawnStringFactory.newDrawnString( "dsName", width*57/100, top1, Alignment.LEFT, false, wtcc_2011_Team_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsTime = drawnStringFactory.newDrawnString( "dsTime", width*97/100, top2, Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), FontColorTimes.getColor(), null, "" );
        dsPos2 = drawnStringFactory.newDrawnString( "dsPos2", width*8/100, top2, Alignment.CENTER, false, wtcc_2011_Race_Numbers_Font.getFont(), isFontAntiAliased(), fontColor1.getColor(), null, "" );
        dsName2 = drawnStringFactory.newDrawnString( "dsName2", width*16/100, top2, Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );
        dsTeam2 = drawnStringFactory.newDrawnString( "dsName", width*57/100, top2, Alignment.LEFT, false, wtcc_2011_Team_Font.getFont(), isFontAntiAliased(), fontColor2.getColor(), null, "" );

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
         
        CurrentSector.update(scoringInfo.getViewedVehicleScoringInfo().getSector());
        
        if(isEditorMode)
        {
            fillvsis(gameData);
            team="BMW 320 TC";
            team2="Seat Leon 1.6";
            return true;
        }
        
        if(RaceInfosWidget.visible() || scoringInfo.getLeadersVehicleScoringInfo().getLapsCompleted() < 1 || scoringInfo.getViewedVehicleScoringInfo().getFinishStatus().isFinished())
        {
            isvisible = false;
            return false;
        }
        
        if(scoringInfo.getSessionNanos() < visibleEnd)
        {
            isvisible = true;
            return true;
        }
            
        
        if( CurrentSector.hasChanged() && scoringInfo.getNumVehicles() > 1)
        {
            if( (int)(Math.random()*frequency.getValue()) == 0 )
            {
                //cpos.update(scoringInfo.getViewedVehicleScoringInfo().getPlace( false ));
                if(!isEditorMode)
                    forceCompleteRedraw( true );
                fillvsis(gameData);
                if(isEditorMode)
                {
                    team="BMW 320 TC";
                    team2="Seat Leon 1.6"; 
                }
                visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
                isvisible = true;
                return true;
            }
        }
        isvisible = false;
        return false;
    		
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        texture.clear( imgPos.getTexture(), offsetX, offsetY, false, null );
        
        texture.clear( imgPos.getTexture(), offsetX, offsetY + height*51/100 , false, null );
        texture.drawImage( texTime, offsetX + width - imgTime.getTexture().getWidth(), offsetY + height*51/100, true, null );

        for(int i=0; i < NumOfPNG; i++)
        {
            if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
            {
                imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*10/100, true, null );
                break;
            }
        }
        
        /*if(team.length() >= 3 && team.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
        {
            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*10/100, true, null );
        }
        else if(team.length() >= 4 && team.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) 
        {
            texManufacturer = imgSeat.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*10/100, true, null );
        }
        else if(team.length() >= 5 && team.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
        {
            texManufacturer = imgVolvo.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*10/100, true, null );
        }
        else if(team.length() >= 9 && team.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
        {
            texManufacturer = imgChevy.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*10/100, true, null );
        }*/
        

        for(int i=0; i < NumOfPNG; i++)
        {
            if(team2.length() >= listPNG[i].length() && team2.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
            {
                imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
                texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*60/100, true, null );
                break;
            }
        }
        
        /*if(team2.length() >= 3 && team2.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
        {
            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*60/100, true, null );
        }
        else if(team2.length() >= 4 && team2.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) 
        {
            texManufacturer = imgSeat.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*60/100, true, null );
        }
        else if(team2.length() >= 5 && team2.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
        {
            texManufacturer = imgVolvo.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*60/100, true, null );
        }
        else if(team2.length() >= 9 && team2.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
        {
            texManufacturer = imgChevy.getImage().getScaledTextureImage( width*7/100, height*30/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*55/100, offsetY + height*60/100, true, null );
        }*/
        
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw  )
        {
            dsPos.draw( offsetX, offsetY, Integer.toString( place ), texture );
            
            dsName.draw( offsetX, offsetY, name.toUpperCase(), texture );
            
            
            dsTime.draw( offsetX, offsetY, gap , texture);
        	dsPos2.draw( offsetX, offsetY, Integer.toString( place2 ), texture );
        	dsName2.draw( offsetX, offsetY, name2.toUpperCase(), texture );
        	
        	
        	
        	//WTCC team part
        	for(int i=0; i < NumOfPNG; i++)
            {
                if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                {
                    if(team.length() == listPNG[i].length())
                        team = "      " + team;
                    else
                        team = "      " + team.substring( listPNG[i].length() );
                    break;
                }
            }
        	
            
        	for(int i=0; i < NumOfPNG; i++)
            {
                if(team2.length() >= listPNG[i].length() && team2.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
                {
                    if(team2.length() == listPNG[i].length())
                        team2 = "      " + team2;
                    else
                        team2 = "      " + team2.substring( listPNG[i].length() );
                    break;
                }
            }
            
            
            
        	dsTeam.draw( offsetX, offsetY, team, texture );
        	dsTeam2.draw( offsetX, offsetY, team2, texture );
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
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( FontColorTimes, "" );
        writer.writeProperty(visibleTime, "");
        writer.writeProperty(frequency, "");
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
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( FontColorTimes ) );
        else if ( loader.loadProperty(visibleTime));
        else if ( loader.loadProperty(frequency));
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
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty( FontColorTimes );
        propsCont.addProperty(visibleTime);
        propsCont.addProperty(frequency);
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
    
    public RaceGapWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011_Race, 51.0f, 4.0f );
        visibleTime = new DelayProperty("visibleTime", net.ctdp.rfdynhud.properties.DelayProperty.DisplayUnits.SECONDS, 15);
        visibleEnd = 0;
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
        
    }
    
}
