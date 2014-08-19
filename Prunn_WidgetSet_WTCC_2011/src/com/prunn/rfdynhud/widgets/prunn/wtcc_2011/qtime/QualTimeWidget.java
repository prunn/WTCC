package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.qtime;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import com.prunn.rfdynhud.plugins.tlcgenerator.StandardTLCGenerator;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
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
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class QualTimeWidget extends Widget
{
    private static enum Situation
    {
        SECTOR_1_FINISHED_BEGIN_SECTOR_2,
        SECTOR_2_FINISHED_BEGIN_SECTOR_3,
        LAP_FINISHED_BEGIN_NEW_LAP,
        OTHER,
        ;
    }
    
    private static final float SECTOR_DELAY = 5f;
    
    private DrawnString dsPos = null;
    private DrawnString dsName = null;
    private DrawnString dsTime = null;
    private DrawnString dsGapS1 = null;
    private DrawnString dsGapS2 = null;
    private DrawnString dsGapS3 = null;
    
    private final ImagePropertyWithTexture imgName = new ImagePropertyWithTexture( "imgName", "prunn/WTCC/qualif.png" );
    private final ImagePropertyWithTexture imgTimeBlack = new ImagePropertyWithTexture( "imgTimeBlack", "prunn/WTCC/qualif_neutral.png" );
    private final ImagePropertyWithTexture imgTimePB = new ImagePropertyWithTexture( "imgTimePB", "prunn/WTCC/qualif_faster.png" );
    private final ImagePropertyWithTexture imgTimeFastest = new ImagePropertyWithTexture( "imgTimeFastest", "prunn/WTCC/qualif_fastest.png" );
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    private TextureImage2D texManufacturer = null;
    private final ImagePropertyWithTexture imgBMW = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/bmw.png" );
    
    
    private final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    private final FontProperty posFont = new FontProperty("positionFont", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_RACE_NUMBERS);
    protected final FontProperty wtcc_2011_Times_Font = new FontProperty("Time Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TIMES);
    private final ColorProperty fontColor1 = new ColorProperty("fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME);
    StandardTLCGenerator gen = new StandardTLCGenerator();
    
    protected final BooleanProperty forcePlayer = new BooleanProperty("Force to player", "forcePlayer", false);
    private int NumOfPNG = 0;
    private String[] listPNG;
    
    private final EnumValue<Situation> situation = new EnumValue<Situation>();
    private final IntValue leaderID = new IntValue();
    private final IntValue leaderPos = new IntValue();
    private final IntValue ownPos = new IntValue();
    private float leadsec1 = -1f;
    private float leadsec2 = -1f;
    private float leadlap = -1f;
    private final FloatValue cursec1 = new FloatValue(-1f, 0.001f);
    private final FloatValue cursec2 = new FloatValue(-1f, 0.001f);
    private final FloatValue cursec3 = new FloatValue(-1f, 0.001f);
    private final FloatValue curlap = new FloatValue(-1f, 0.001f);
    private final FloatValue oldbesttime = new FloatValue(-1f, 0.001f);
    private final FloatValue lastLaptime = new FloatValue(-1f, 0.001f);
    private final FloatValue fastestlap = new FloatValue(-1f, 0.001f);
    private final FloatValue knockoutlap = new FloatValue(-1f, 0.001f);
    //private float oldbest = 0;
    private static Boolean isvisible = false;
    public static Boolean visible()
    {
        return isvisible;
    }
    
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        String cpid = "Y29weXJpZ2h0QFBydW5uMjAxMQ";
        if(!isEditorMode)
            log(cpid);
        situation.reset();
        leaderID.reset();
        leaderPos.reset();
        ownPos.reset();
        cursec1.reset();
        cursec2.reset();
        cursec3.reset();
        curlap.reset();
        oldbesttime.reset();
        lastLaptime.reset();
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
        int fh = TextureImage2D.getStringHeight( "09gy", getFontProperty() );
        int fh_times = TextureImage2D.getStringHeight( "09gy", wtcc_2011_Times_Font );
        imgName.updateSize( width*55/100, height, isEditorMode );
        imgTimeBlack.updateSize( width*15/100, height, isEditorMode );
        imgTimeFastest.updateSize( width*15/100, height, isEditorMode );
        imgTimePB.updateSize( width*15/100, height, isEditorMode );
        
        Color whiteFontColor = fontColor2.getColor();
        
        int textOff = ( height - fh ) / 2;
        int textOff_times = ( height - fh_times ) / 2;
        
        dsName = drawnStringFactory.newDrawnString( "dsName", width*9/100, textOff + fontyoffset.getValue(), Alignment.LEFT, false, getFont(), isFontAntiAliased(), whiteFontColor);
        dsTime = drawnStringFactory.newDrawnString( "dsTime", width*53/100, textOff_times + fontyoffset.getValue(), Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), whiteFontColor);
        dsPos = drawnStringFactory.newDrawnString( "dsPos", width*9/200, textOff + fontyoffset.getValue(), Alignment.CENTER, false, posFont.getFont(), isFontAntiAliased(), fontColor1.getColor());
        dsGapS1 = drawnStringFactory.newDrawnString( "dsTime", width*67/100, textOff_times + fontyoffset.getValue(), Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), whiteFontColor);
        dsGapS2 = drawnStringFactory.newDrawnString( "dsTime", width*82/100, textOff_times + fontyoffset.getValue(), Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), whiteFontColor);
        dsGapS3 = drawnStringFactory.newDrawnString( "dsTime", width*97/100, textOff_times + fontyoffset.getValue(), Alignment.RIGHT, false, wtcc_2011_Times_Font.getFont(), isFontAntiAliased(), whiteFontColor);

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
    
    private void updateSectorValues( ScoringInfo scoringInfo )
    {
        VehicleScoringInfo currentcarinfos = scoringInfo.getViewedVehicleScoringInfo();
        VehicleScoringInfo leadercarinfos = scoringInfo.getLeadersVehicleScoringInfo();
        if(forcePlayer.getValue())
            currentcarinfos = scoringInfo.getPlayersVehicleScoringInfo();
        
        if(leadercarinfos.getFastestLaptime() != null && leadercarinfos.getFastestLaptime().getLapTime() >= 0)
        {
            leadsec1 = leadercarinfos.getFastestLaptime().getSector1();
            leadsec2 = leadercarinfos.getFastestLaptime().getSector1And2();
            leadlap = leadercarinfos.getFastestLaptime().getLapTime();
        }
        else
        {
            leadsec1 = 0f;
            leadsec2 = 0f;
            leadlap = 0f;
        }
        
        cursec1.update( currentcarinfos.getCurrentSector1() );
        cursec2.update( currentcarinfos.getCurrentSector2( true ) );
        cursec3.update( currentcarinfos.getLastLapTime() );

        if ( scoringInfo.getSessionTime() > 0f )
            curlap.update( currentcarinfos.getCurrentLaptime() );
        else
            curlap.update( scoringInfo.getSessionNanos() / 1000000000f - currentcarinfos.getLapStartTime() ); 
            

    }
    
    private boolean updateSituation( VehicleScoringInfo currentcarinfos )
    {
        final byte sector = currentcarinfos.getSector();
        
        if(sector == 2 && curlap.getValue() - cursec1.getValue() <= SECTOR_DELAY && leadlap > 0)
        {
            situation.update( Situation.SECTOR_1_FINISHED_BEGIN_SECTOR_2 );
        }
        else if(sector == 3 && curlap.getValue() - cursec2.getValue() <= SECTOR_DELAY && leadlap > 0)
        {
            situation.update( Situation.SECTOR_2_FINISHED_BEGIN_SECTOR_3 );
        }
        else if(sector == 1 && curlap.getValue() <= SECTOR_DELAY && currentcarinfos.getLastLapTime() > 0)
        {
            situation.update( Situation.LAP_FINISHED_BEGIN_NEW_LAP );
        }
        else
        {
            situation.update( Situation.OTHER );
        }
        
        return ( situation.hasChanged() );
    }
    
    @Override
    protected Boolean updateVisibility(LiveGameData gameData, boolean isEditorMode)
    {
        
        super.updateVisibility(gameData, isEditorMode);
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        updateSectorValues( scoringInfo );
        VehicleScoringInfo currentcarinfos = scoringInfo.getViewedVehicleScoringInfo();
        if(forcePlayer.getValue())
            currentcarinfos = scoringInfo.getPlayersVehicleScoringInfo();
        
        
        
        fastestlap.update(scoringInfo.getLeadersVehicleScoringInfo().getBestLapTime());
        //if( gameData.getScoringInfo().getNumVehicles() >= posKnockout.getValue() )
        //    knockoutlap.update(scoringInfo.getVehicleScoringInfo( posKnockout.getValue()-1 ).getBestLapTime());
        
        if ( (updateSituation( currentcarinfos )  || fastestlap.hasChanged() || knockoutlap.hasChanged()) && !isEditorMode)
            forceCompleteRedraw( true );
        
        if ( currentcarinfos.isInPits() )
        {
            isvisible = false;
            return false;
        }
        
        if(currentcarinfos.getFinishStatus() == FinishStatus.FINISHED && situation.getValue() != Situation.LAP_FINISHED_BEGIN_NEW_LAP )
            return false;
        
        float curLaptime;
        if ( scoringInfo.getSessionTime() > 0f )
            curLaptime = currentcarinfos.getCurrentLaptime();
        else
            curLaptime = scoringInfo.getSessionNanos() / 1000000000f - currentcarinfos.getLapStartTime();
        
        if ( curLaptime > 0f )
        {
            isvisible = true;
            //forceCompleteRedraw( true );
            return true;
        }
            
        return false;
         
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo currentcarinfos = scoringInfo.getViewedVehicleScoringInfo();
        //VehicleScoringInfo leadercarinfos = scoringInfo.getLeadersVehicleScoringInfo();
        if(forcePlayer.getValue())
            currentcarinfos = scoringInfo.getPlayersVehicleScoringInfo();
        
        
        texture.clear( imgName.getTexture(), offsetX, offsetY, false, null );
        
        
        switch ( situation.getValue() )
        {
            case LAP_FINISHED_BEGIN_NEW_LAP:
                
                if(currentcarinfos.getLastSector1() <= leadsec1)
                    texture.clear( imgTimeFastest.getTexture(), offsetX+width*55/100, offsetY, false, null );
                else
                    if(currentcarinfos.getLastSector1() <= currentcarinfos.getFastestLaptime().getSector1())
                        texture.clear( imgTimePB.getTexture(), offsetX+width*55/100, offsetY, false, null );
                    else
                        texture.clear( imgTimeBlack.getTexture(), offsetX+width*55/100, offsetY, false, null );
                
                if(currentcarinfos.getLastSector2( true ) <= leadsec2)
                    texture.clear( imgTimeFastest.getTexture(), offsetX+width*70/100, offsetY, false, null );
                else
                    if(currentcarinfos.getLastSector2( true ) <= currentcarinfos.getFastestLaptime().getSector1And2())
                        texture.clear( imgTimePB.getTexture(), offsetX+width*70/100, offsetY, false, null );
                    else
                        texture.clear( imgTimeBlack.getTexture(), offsetX+width*70/100, offsetY, false, null );
                
                if(cursec3.getValue() <= leadlap)
                    texture.clear( imgTimeFastest.getTexture(), offsetX+width*85/100, offsetY, false, null );
                else
                    if(cursec3.getValue() <= currentcarinfos.getFastestLaptime().getLapTime())
                        texture.clear( imgTimePB.getTexture(), offsetX+width*85/100, offsetY, false, null );
                    else
                        texture.clear( imgTimeBlack.getTexture(), offsetX+width*85/100, offsetY, false, null );
                
                        
                break;
                
            default:
                
                if(currentcarinfos.getSector() == 3 && leadsec1 > 0)
                {    
                    if(cursec2.getValue() <= leadsec2)
                        texture.clear( imgTimeFastest.getTexture(), offsetX+width*70/100, offsetY, false, null );
                    else
                        if(currentcarinfos.getFastestLaptime() != null && cursec2.getValue() <= currentcarinfos.getFastestLaptime().getSector1And2())
                            texture.clear( imgTimePB.getTexture(), offsetX+width*70/100, offsetY, false, null );
                        else
                            texture.clear( imgTimeBlack.getTexture(), offsetX+width*70/100, offsetY, false, null );
                    
                    if(cursec1.getValue() <= leadsec1)
                        texture.clear( imgTimeFastest.getTexture(), offsetX+width*55/100, offsetY, false, null );
                    else
                        if(currentcarinfos.getFastestLaptime() != null && cursec1.getValue() <= currentcarinfos.getFastestLaptime().getSector1())
                            texture.clear( imgTimePB.getTexture(), offsetX+width*55/100, offsetY, false, null );
                        else
                            texture.clear( imgTimeBlack.getTexture(), offsetX+width*55/100, offsetY, false, null );
                }
                else
                    if(currentcarinfos.getSector() == 2 && leadsec1 > 0)
                    {
                        if(cursec1.getValue() <= leadsec1)
                            texture.clear( imgTimeFastest.getTexture(), offsetX+width*55/100, offsetY, false, null );
                        else
                            if(currentcarinfos.getFastestLaptime() != null && cursec1.getValue() <= currentcarinfos.getFastestLaptime().getSector1())
                                texture.clear( imgTimePB.getTexture(), offsetX+width*55/100, offsetY, false, null );
                            else
                                texture.clear( imgTimeBlack.getTexture(), offsetX+width*55/100, offsetY, false, null );
                        
                    } 
                break;
                
         }
        
        String team;
        if(isEditorMode)
            team = "Seat C30";
        else if(currentcarinfos.getVehicleInfo() != null)
            team = currentcarinfos.getVehicleInfo().getManufacturer();
        else
            team = currentcarinfos.getVehicleClass();

        for(int i=0; i < NumOfPNG; i++)
        {
            if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
            {
                imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                texManufacturer = imgBMW.getImage().getScaledTextureImage( width*5/100, height*65/100, texManufacturer, isEditorMode );
                texture.drawImage( texManufacturer, offsetX + width*35/100, offsetY + height*20/100, true, null );
                break;
            }
        }
        
        /*if(team.length() >= 3 && team.substring( 0, 3 ).toUpperCase().equals( "BMW" )) 
        {
            texManufacturer = imgBMW.getImage().getScaledTextureImage( width*5/100, height*65/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*35/100, offsetY + height*20/100, true, null );
        }
        else if(team.length() >= 4 && team.substring( 0, 4 ).toUpperCase().equals( "SEAT" )) 
        {
            texManufacturer = imgSeat.getImage().getScaledTextureImage( width*5/100, height*65/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*35/100, offsetY + height*20/100, true, null );
        }
        else if(team.length() >= 5 && team.substring( 0, 5 ).toUpperCase().equals( "VOLVO" )) 
        {
            texManufacturer = imgVolvo.getImage().getScaledTextureImage( width*5/100, height*65/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*35/100, offsetY + height*20/100, true, null );
        }
        else if(team.length() >= 9 && team.substring( 0, 9 ).toUpperCase().equals( "CHEVROLET" )) 
        {
            texManufacturer = imgChevy.getImage().getScaledTextureImage( width*5/100, height*65/100, texManufacturer, isEditorMode );
            texture.drawImage( texManufacturer, offsetX + width*35/100, offsetY + height*20/100, true, null );
        }*/
        
    }
    
    private static final String getTimeAsGapString2( float gap )
    {
        if ( gap == 0f )
            return ( "- " + TimingUtil.getTimeAsLaptimeString( 0f ) );
        
        if ( gap < 0f )
            return ( "- " + TimingUtil.getTimeAsLaptimeString( -gap ) );
        
        return ( "+ " + TimingUtil.getTimeAsLaptimeString( gap ) );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        updateSectorValues( scoringInfo );
        
        VehicleScoringInfo currentcarinfos = scoringInfo.getViewedVehicleScoringInfo();
        VehicleScoringInfo leadercarinfos = scoringInfo.getLeadersVehicleScoringInfo();
        
        if(forcePlayer.getValue())
            currentcarinfos = scoringInfo.getPlayersVehicleScoringInfo();
            
        leaderID.update( leadercarinfos.getDriverId() );
        leaderPos.update( leadercarinfos.getPlace( false ) );
        
        if ( needsCompleteRedraw || ( clock.c() && leaderID.hasChanged() ) )
        {
            dsName.draw( offsetX, offsetY, gen.ShortNameWTCC( currentcarinfos.getDriverName().toUpperCase() ), texture );
            
        }
        ownPos.update( currentcarinfos.getPlace( false ) );
        
        if ( needsCompleteRedraw || ( clock.c() && ownPos.hasChanged() ) )
            dsPos.draw( offsetX, offsetY, ownPos.getValueAsString(), texture );
        
        
        switch ( situation.getValue() )
        {
            case SECTOR_1_FINISHED_BEGIN_SECTOR_2:
                
                if ( needsCompleteRedraw || ( clock.c() && cursec1.hasChanged() ) )
                    dsTime.draw( offsetX, offsetY, TimingUtil.getTimeAsString( cursec1.getValue(), false, false, true, true ) , texture);
                if(leadsec1 > 0)
                    dsGapS1.draw( offsetX, offsetY, getTimeAsGapString2( cursec1.getValue() - leadsec1), texture);
                
                break;
                
            case SECTOR_2_FINISHED_BEGIN_SECTOR_3:
                
                if ( needsCompleteRedraw || ( clock.c() && cursec2.hasChanged() ) )
                    dsTime.draw( offsetX, offsetY, TimingUtil.getTimeAsString( cursec2.getValue(), false, false, true, true ) , texture);
                
                if(leadsec1 > 0)
                {
                    dsGapS1.draw( offsetX, offsetY, getTimeAsGapString2( cursec1.getValue() - leadsec1), texture);
                    dsGapS2.draw( offsetX, offsetY, getTimeAsGapString2(cursec2.getValue() - leadsec2), texture);
                }
                
                break;
                
            case LAP_FINISHED_BEGIN_NEW_LAP:
                oldbesttime.update( currentcarinfos.getBestLapTime() );
                
                //if(oldbesttime.hasChanged())
                //    oldbest = oldbesttime.getOldValue();
                
                lastLaptime.update( currentcarinfos.getLastLapTime() );
                
                
                if ( needsCompleteRedraw || ( clock.c() && lastLaptime.hasChanged() ) )
                    dsTime.draw( offsetX, offsetY, TimingUtil.getTimeAsString( lastLaptime.getValue(), false, false, true, true ) , texture);
                
                if(leadsec1 > 0)
                {
                    dsGapS1.draw( offsetX, offsetY, getTimeAsGapString2( currentcarinfos.getLastSector1() - leadsec1), texture);
                    dsGapS2.draw( offsetX, offsetY, getTimeAsGapString2(currentcarinfos.getLastSector2( true ) - leadsec2), texture);
                    dsGapS3.draw( offsetX, offsetY, getTimeAsGapString2(cursec3.getValue() - leadlap), texture);
                }
                
                break;
              
            case OTHER:
                // other cases not info not drawn
                
                if ( needsCompleteRedraw || ( clock.c() && curlap.hasChanged() ) )
                    dsTime.draw( offsetX, offsetY, TimingUtil.getTimeAsString( curlap.getValue(), false, false, true, false ) + "    ", texture);
                
                if(currentcarinfos.getSector() == 3 && leadsec1 > 0)
                {    
                    dsGapS1.draw( offsetX, offsetY, getTimeAsGapString2( cursec1.getValue() - leadsec1), texture);
                    dsGapS2.draw( offsetX, offsetY, getTimeAsGapString2(cursec2.getValue() - leadsec2), texture);
                }
                else
                    if(currentcarinfos.getSector() == 2 && leadsec1 > 0)
                        dsGapS1.draw( offsetX, offsetY, getTimeAsGapString2( cursec1.getValue() - leadsec1), texture);
                    
                break;
        }
        
               
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( wtcc_2011_Times_Font, "" );
        writer.writeProperty( posFont, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontyoffset, "" );
        writer.writeProperty( forcePlayer, "" );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( wtcc_2011_Times_Font ) );
        else if ( loader.loadProperty( posFont ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontyoffset ) );
        else if ( loader.loadProperty( forcePlayer ) );
    }
    
    @Override
    protected void addFontPropertiesToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Colors and Fonts" );
        
        super.addFontPropertiesToContainer( propsCont, forceAll );
        
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty( wtcc_2011_Times_Font );
        propsCont.addProperty( posFont );
        
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        propsCont.addProperty( forcePlayer );
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
    
    public QualTimeWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 26.2f, 10.75f );
        
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
    }
    
}
