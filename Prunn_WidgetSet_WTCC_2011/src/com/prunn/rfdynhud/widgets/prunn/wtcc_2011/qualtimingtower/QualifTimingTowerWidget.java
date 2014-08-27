package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.qualtimingtower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
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
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.StandingsTools;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.StandingsView;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class QualifTimingTowerWidget extends Widget
{
    private DrawnString[] dsPos = null;
    private DrawnString[] dsName = null;
    private DrawnString[] dsTime = null;
    private TextureImage2D texPit = null;
    private final ImagePropertyWithTexture imgFirst = new ImagePropertyWithTexture( "imgFirst", "prunn/WTCC/tower/bg_first.png" );
    private final ImagePropertyWithTexture imgPos = new ImagePropertyWithTexture( "imgPos", "prunn/WTCC/tower/bg.png" );
    private final ImagePropertyWithTexture imgPosNew = new ImagePropertyWithTexture( "imgPosNew", "prunn/WTCC/tower/bg_newlap.png" );
    private final ImagePropertyWithTexture imgPosKnockOut = new ImagePropertyWithTexture( "imgPosKnockOut", "prunn/WTCC/tower/bg_knockout.png" );
    private final ImagePropertyWithTexture imgPit = new ImagePropertyWithTexture( "imgPit", "prunn/WTCC/tower/InPit.png" );
    private final ImagePropertyWithTexture imgFinish = new ImagePropertyWithTexture( "imgFinish", "prunn/WTCC/tower/finished.png" );
    
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    protected final FontProperty wtcc_2011_Times_Font_tower = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_TIMES_TOWER);
    protected final FontProperty wtcc_2011_Race_Numbers_Font = new FontProperty("Pos Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_RACE_NUMBERS_TOWER);
    private final ColorProperty fontColor1 = new ColorProperty( "fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    private final ColorProperty fontColor2 = new ColorProperty( "fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME );
    private final ColorProperty KnockoutFontColor = new ColorProperty("Knockout Font Color", PrunnWidgetSet_wtcc_2011.FONT_COLOR4_NAME);
    private ColorProperty drawnFontColor;
    private final IntProperty numVeh = new IntProperty( "numberOfVehicles", 8 );
    private IntProperty knockoutQual = new IntProperty("Knockout position Qual", 10);
    private IntProperty knockoutFP1 = new IntProperty("Knockout position FP1", 16);
    private IntProperty knockoutFP2 = new IntProperty("Knockout position FP2", 16);
    private IntProperty knockoutFP3 = new IntProperty("Knockout position FP3", 16);
    private IntProperty knockoutFP4 = new IntProperty("Knockout position FP4", 16);
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    private IntProperty fontxposoffset = new IntProperty("X Position Font Offset", 0);
    private IntProperty fontxnameoffset = new IntProperty("X Name Font Offset", 0);
    private IntProperty fontxtimeoffset = new IntProperty("X Time Font Offset", 0);
    
    private final DelayProperty visibleTime = new DelayProperty( "visibleTime", DelayProperty.DisplayUnits.SECONDS, 5 );
    private final DelayProperty visibleTimeButton = new DelayProperty( "visibleTimeButton", DelayProperty.DisplayUnits.SECONDS, 15 );
    private long visibleEnd = -1L;
    private long[] visibleEndArray;
    
    private VehicleScoringInfo[] vehicleScoringInfos;
    private IntValue[] positions = null;
    private final IntValue numValid = new IntValue();
    private StringValue[] driverNames = null;
    private FloatValue[] gaps = null;
    private BoolValue[] IsInPit = null;
    private BoolValue[] IsFinished = null;
    private int knockout;
    private int[] driverIDs = null;
    private boolean[] gapFlag = null;
    private boolean[] gapFlag2 = null;
    private static final InputAction showTimes = new InputAction( "Show times", true );
    private final IntValue inputShowTimes = new IntValue();
    private BooleanProperty AbsTimes = new BooleanProperty("Use absolute times", false) ;
    
    
    @Override
    public InputAction[] getInputActions()
    {
        return ( new InputAction[] { showTimes } );
    }
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        String cpid = "Y29weXJpZ2h0QFBydW5uMjAxMQ";
        if(!isEditorMode)
            log(cpid);
        visibleEnd = -1L;
        numValid.reset();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
        int maxNumItems = numVeh.getValue();
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( action == showTimes )
        {
            for(int i = 1; i < maxNumItems;i++)
                visibleEndArray[i] = scoringInfo.getSessionNanos() + visibleTimeButton.getDelayNanos();
            
            visibleEnd = scoringInfo.getSessionNanos() + visibleTimeButton.getDelayNanos();
            inputShowTimes.update( inputShowTimes.getValue()+1 );
        }
        
        return ( result );
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
        gapFlag = new boolean[maxNumItems];
        gapFlag2 = new boolean[maxNumItems];
        positions = new IntValue[maxNumItems];
        driverNames = new StringValue[maxNumItems];
        IsInPit = new BoolValue[maxNumItems];
        IsFinished = new BoolValue[maxNumItems];
        driverIDs = new int[maxNumItems];
        visibleEndArray = new long[maxNumItems];
        vehicleScoringInfos = new VehicleScoringInfo[maxNumItems];
        
        for(int i=0;i < maxNumItems;i++)
        { 
            IsInPit[i] = new BoolValue();
            IsFinished[i] = new BoolValue();
            positions[i] = new IntValue();
            driverNames[i] = new StringValue();
            gaps[i] = new FloatValue();
        }
        
        
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int maxNumItems = numVeh.getValue();
        int fh = TextureImage2D.getStringHeight( "0%C", getFontProperty() );
        int rowHeight = height / maxNumItems;
        
        imgPosNew.updateSize( Math.round(width - rowHeight), rowHeight, isEditorMode );
        imgFirst.updateSize( Math.round(width * 0.5f), rowHeight, isEditorMode );
        imgPos.updateSize( Math.round(width * 0.5f), rowHeight, isEditorMode );
        imgPosKnockOut.updateSize( Math.round(width * 0.5f), rowHeight, isEditorMode );
        
        dsPos = new DrawnString[maxNumItems];
        dsName = new DrawnString[maxNumItems];
        dsTime = new DrawnString[maxNumItems];
        
        int top = ( rowHeight - fh ) / 2;
        
        for(int i=0;i < maxNumItems;i++)
        {
            dsPos[i] = drawnStringFactory.newDrawnString( "dsPos", width*12/100 + fontxposoffset.getValue(), top + 3 + fontyoffset.getValue(), Alignment.CENTER, false, wtcc_2011_Race_Numbers_Font.getFont(), isFontAntiAliased(), fontColor2.getColor() );
            dsName[i] = drawnStringFactory.newDrawnString( "dsName", width*24/100 + fontxnameoffset.getValue(), top + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor() );
            dsTime[i] = drawnStringFactory.newDrawnString( "dsTime", width*85/100 + fontxtimeoffset.getValue(), top + fontyoffset.getValue(), Alignment.RIGHT, false, wtcc_2011_Times_Font_tower.getFont(), isFontAntiAliased(), fontColor2.getColor() );
            
            top += rowHeight;
        }
        
        switch(gameData.getScoringInfo().getSessionType())
        {
            case QUALIFYING1: case QUALIFYING2: case QUALIFYING3: case QUALIFYING4:
                knockout = knockoutQual.getValue();
                break;
            case PRACTICE1:
                knockout = knockoutFP1.getValue();
                break;
            case PRACTICE2:
                knockout = knockoutFP2.getValue();
                break;
            case PRACTICE3:
                knockout = knockoutFP3.getValue();
                break;
            case PRACTICE4:
                knockout = knockoutFP4.getValue();
                break;
            default:
                knockout = 100;
                break;
        }
        if(isEditorMode)
            knockout = knockoutQual.getValue();
        
        texPit = imgPit.getImage().getScaledTextureImage( width*17/100, rowHeight, texPit, isEditorMode );
        
    }
    public String generateThreeLetterCode2( String driverName, java.io.File getConfigFolder )
    {
        
        //check if name is in ini file
        File ini;
        //ini = new File(gameData.getFileSystem().getConfigFolder(), "three_letter_codes.ini");
        ini = new File(getConfigFolder, "three_letter_codes.ini");
        
        
        if(ini.exists())
        {    
            try
            {
                int delimiter;
                String line;
                String fromFileTLC="";
                BufferedReader br = new BufferedReader( new FileReader( ini ) );
                
                while ((line = br.readLine()) != null)
                {   
                    delimiter = line.lastIndexOf( '=' );
                    
                    if(driverName.toUpperCase().equals(line.substring( 0, delimiter ).toUpperCase()))
                    {
                        fromFileTLC = line.substring( line.length() - 3, line.length() ).toUpperCase();
                        //RFDHLog.exception( "TLC:" + fromFileTLC ) ;
                        return fromFileTLC;
                    }
                           
                        
                }
                
            }
            catch ( Throwable t )
            {
               
            }
        }
        else
            RFDHLog.exception( "WARNING: No three_letter_codes.ini found." );
        
        
        if ( driverName.length() <= 3 )
        {
            return ( driverName.toUpperCase() );
        }
        
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            return ( driverName.substring( 0, 3 ).toUpperCase() );
        }
        
        String tlc = driverName.substring( sp + 1, Math.min( sp + 4, driverName.length() ) ).toUpperCase();
        
        return ( tlc );
    }
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        super.updateVisibility( gameData, isEditorMode );
        
        initValues();
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        int drawncars = Math.min( scoringInfo.getNumVehicles(), numVeh.getValue() );
        VehicleScoringInfo  comparedVSI = scoringInfo.getViewedVehicleScoringInfo();
        
        if(inputShowTimes.hasChanged())
            forceCompleteRedraw( true ); 
        if(scoringInfo.getViewedVehicleScoringInfo().getBestLapTime() > 0)
        {
            if(scoringInfo.getViewedVehicleScoringInfo().getPlace( false ) > numVeh.getValue())
                comparedVSI = scoringInfo.getVehicleScoringInfo( scoringInfo.getViewedVehicleScoringInfo().getPlace( false ) - 5 );
            else
                comparedVSI = scoringInfo.getLeadersVehicleScoringInfo();
        
        }
        else
        {
            comparedVSI = scoringInfo.getLeadersVehicleScoringInfo();
        }

        StandingsTools.getDisplayedVSIsForScoring(scoringInfo, comparedVSI, false, StandingsView.RELATIVE_TO_LEADER, true, vehicleScoringInfos);
        
        for(int i=0;i < drawncars;i++)
        { 
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            
            if(vsi != null && vsi.getFinishStatus() != FinishStatus.DQ)
            {

                positions[i].update( vsi.getPlace( false ) );
                driverNames[i].update(generateThreeLetterCode2( vsi.getDriverName(), gameData.getFileSystem().getConfigFolder() ));
                IsInPit[i].update( vsi.isInPits() );
                
                if(vsi.getFinishStatus() == FinishStatus.FINISHED)
                    IsFinished[i].update( true );
                else
                    IsFinished[i].update( false );
                
                gaps[i].setUnchanged();
                gaps[i].update(vsi.getBestLapTime());
                gapFlag[i] = gaps[i].hasChanged( false ) || isEditorMode;
                gapFlag2[i] = gapFlag[i];
                if((IsInPit[i].hasChanged() || IsFinished[i].hasChanged()) && !isEditorMode)
                    forceCompleteRedraw( true );  
            }
        }
        
        if((scoringInfo.getSessionNanos() >= visibleEnd) && (visibleEnd != -1L))
        {
            visibleEnd = -1L;
            if ( !isEditorMode )
                forceCompleteRedraw( true );
        }
        
        if(!gaps[0].isValid())
            visibleEnd = -1L;
        else if(gapFlag[0])
            visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
        
        for(int i=1;i < drawncars;i++)
        {
            if(gaps[i].isValid())
            {
                if(gapFlag[i] && !isEditorMode )
                {
                    //search if the time really changed or just the position before redrawing
                    for(int j=0;j < drawncars; j++)
                    {
                        if ( vehicleScoringInfos[i].getDriverId() == driverIDs[j] )
                        {
                            if(gaps[i].getValue() == gaps[j].getOldValue())
                            {
                                gapFlag[i] = false;
                                break;
                            }
                        }
                    }
                }
                
                if((scoringInfo.getSessionNanos() >= visibleEndArray[i]) && (visibleEndArray[i] != -1L))
                {
                    visibleEndArray[i] = -1L;
                    if ( !isEditorMode )
                        forceCompleteRedraw( true );
                }
                
                if(gapFlag[i]) 
                {
                    visibleEndArray[i] = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
                    if ( !isEditorMode )
                        forceCompleteRedraw( true );
                }
            }
        }
        
        for(int i=0;i < drawncars;i++)
        { 
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            
            if(vsi != null)
            {
                driverIDs[i] = vsi.getDriverId();
            }
        }
        
        int nv = 0;
        for(int i=0;i < drawncars;i++)
        {
            if(gaps[i].isValid())
                nv++;
        }
        
        numValid.update( nv );
        if ( numValid.hasChanged() && !isEditorMode )
            forceCompleteRedraw( true );
        
        if( gameData.getScoringInfo().getLeadersVehicleScoringInfo().getBestLapTime() > 0 || isEditorMode)
        {
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
        int drawncars = Math.min( scoringInfo.getNumVehicles(), maxNumItems );
        int rowHeight = height / maxNumItems;
        
        if(gaps[0].isValid())
        {
            if(scoringInfo.getSessionNanos() < visibleEnd )
            {
                texture.clear( imgPosNew.getTexture(), offsetX, offsetY, false, null );
                if(IsInPit[0].getValue() || isEditorMode)
                {
                    texPit = imgPit.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                    texture.drawImage( texPit, offsetX + width - texPit.getWidth(), offsetY, false, null );
                }
                else
                    if(IsFinished[0].getValue())
                    {
                        texPit = imgFinish.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                        texture.drawImage( texPit, offsetX + width - texPit.getWidth(), offsetY, false, null );
                    }
            }
            else
            {
                texture.clear( imgFirst.getTexture(), offsetX, offsetY, false, null );
                if(IsInPit[0].getValue() || isEditorMode)
                {
                    texPit = imgPit.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                    texture.drawImage( texPit, offsetX + width*50/100, offsetY, false, null );
                }
                else
                    if(IsFinished[0].getValue())
                    {
                        texPit = imgFinish.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                        texture.drawImage( texPit, offsetX + width*50/100, offsetY, false, null );
                    }
            }
          
            
            
            
            
        }
        
        
        for(int i=1;i < drawncars;i++)
        {
            if(gaps[i].isValid())
            {
                if(scoringInfo.getSessionNanos() < visibleEndArray[i] || isEditorMode)
                {
                    texture.clear( imgPosNew.getTexture(), offsetX, offsetY+rowHeight*i, false, null );
                    
                    if(IsInPit[i].getValue())
                    {
                        texPit = imgPit.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                        texture.drawImage( texPit, offsetX + width - texPit.getWidth(), offsetY+rowHeight*i, false, null );
                    }
                    else
                        if(IsFinished[i].getValue() || isEditorMode)
                        {
                            texPit = imgFinish.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                            texture.drawImage( texPit, offsetX + width - texPit.getWidth(), offsetY+rowHeight*i, false, null );
                        }
                }
                else  
                    {
                        if(positions[i].getValue() <= knockout)
                            texture.clear( imgPos.getTexture(), offsetX, offsetY+rowHeight*i, false, null );
                        else
                            texture.clear( imgPosKnockOut.getTexture(), offsetX, offsetY+rowHeight*i, false, null );
                        
                        if(IsInPit[i].getValue() )
                        {
                            texPit = imgPit.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                            texture.drawImage( texPit, offsetX + width*50/100, offsetY+rowHeight*i, false, null );
                        }
                        else
                            if(IsFinished[i].getValue() || isEditorMode)
                            {
                                texPit = imgFinish.getImage().getScaledTextureImage( rowHeight, rowHeight, texPit, isEditorMode );
                                texture.drawImage( texPit, offsetX + width*50/100, offsetY+rowHeight*i, false, null );
                            }
                    
                    }
            }
            
        }
        
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int drawncars = Math.min( gameData.getScoringInfo().getNumVehicles(), numVeh.getValue() );
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        //one time for leader
        
        if ( needsCompleteRedraw || ( clock.c() && gapFlag2[0]))
        {
            if(gaps[0].isValid())
            {
                dsPos[0].draw( offsetX, offsetY, positions[0].getValueAsString(), texture );
                dsName[0].draw( offsetX, offsetY, driverNames[0].getValue(), texture );
                
                if(scoringInfo.getSessionNanos() < visibleEnd )
                    dsTime[0].draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString(gaps[0].getValue() ) , texture);
                else
                    dsTime[0].draw( offsetX, offsetY, "" , texture);
            }
            else
            {
                dsTime[0].draw( offsetX, offsetY, "" , texture);
            }
            
            gapFlag2[0] = false;
            
        }
        
        // the other guys
        for(int i=1;i < drawncars;i++)
        { 
            if ( needsCompleteRedraw || ( clock.c() && gapFlag2[i]))
            {
                if(gaps[i].isValid())
                {
                    if(positions[i].getValue() >= 2 && positions[i].getValue() <= 10 && scoringInfo.getSessionNanos() >= visibleEndArray[i] )
                        drawnFontColor = fontColor1;
                    else
                        drawnFontColor = fontColor2;
                    
                    dsPos[i].draw( offsetX, offsetY, positions[i].getValueAsString(), drawnFontColor.getColor(), texture );
                    dsName[i].draw( offsetX, offsetY,driverNames[i].getValue() , texture );  
                    
                    
                    
                    if(scoringInfo.getSessionNanos() < visibleEndArray[i])
                    {
                        if(AbsTimes.getValue())
                            dsTime[i].draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString(gaps[i].getValue() ), texture);
                        else
                            dsTime[i].draw( offsetX, offsetY,"+ " + TimingUtil.getTimeAsLaptimeString(Math.abs( gaps[i].getValue() - gaps[0].getValue() )) , texture);
                    }
                    else
                        dsTime[i].draw( offsetX, offsetY,"", texture);
                    
                    
                }
                
                gapFlag2[i] = false;
                
                
            }
            
        }
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( wtcc_2011_Times_Font_tower, "" );
        writer.writeProperty( wtcc_2011_Race_Numbers_Font, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( numVeh, "" );
        writer.writeProperty( visibleTime, "" );
        writer.writeProperty( visibleTimeButton, "" );
        writer.writeProperty( AbsTimes, "" );
        writer.writeProperty( KnockoutFontColor, "" );
        writer.writeProperty( knockoutQual, "" );
        writer.writeProperty( knockoutFP1, "" );
        writer.writeProperty( knockoutFP2, "" );
        writer.writeProperty( knockoutFP3, "" );
        writer.writeProperty( knockoutFP4, "" );
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
        else if ( loader.loadProperty( wtcc_2011_Times_Font_tower ) );
        else if ( loader.loadProperty( wtcc_2011_Race_Numbers_Font ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( numVeh ) );
        else if ( loader.loadProperty( visibleTime ) );
        else if ( loader.loadProperty( visibleTimeButton ) );
        else if ( loader.loadProperty( AbsTimes ) );
        else if ( loader.loadProperty( knockoutQual ) );
        else if ( loader.loadProperty( knockoutFP1 ) );
        else if ( loader.loadProperty( knockoutFP2 ) );
        else if ( loader.loadProperty( knockoutFP3 ) );
        else if ( loader.loadProperty( knockoutFP4 ) );
        else if ( loader.loadProperty( KnockoutFontColor ) );
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
        propsCont.addProperty( wtcc_2011_Times_Font_tower );
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
        propsCont.addProperty( visibleTime );
        propsCont.addProperty( visibleTimeButton );
        propsCont.addProperty( AbsTimes );
        propsCont.addGroup( "Knockout Infos" );
        propsCont.addProperty( knockoutQual );
        propsCont.addProperty( knockoutFP1 );
        propsCont.addProperty( knockoutFP2 );
        propsCont.addProperty( knockoutFP3 );
        propsCont.addProperty( knockoutFP4 );
        propsCont.addProperty( KnockoutFontColor );
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
        
        //getFontProperty().setFont( "Dialog", Font.PLAIN, 6, false, true );
    }
    
    public QualifTimingTowerWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 22.5f, 32.5f );
        
        //getBackgroundProperty().setColorValue( "#00000000" );
        //getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
        //getFontColorProperty().setColor( PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    }
}
