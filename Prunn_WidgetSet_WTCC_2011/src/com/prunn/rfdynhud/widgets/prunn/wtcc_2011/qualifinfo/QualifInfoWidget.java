package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.qualifinfo;

import java.awt.Font;
import java.io.File;
import java.io.IOException;

import com.prunn.rfdynhud.plugins.tlcgenerator.StandardTLCGenerator;
import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;
//import com.prunn.rfdynhud.widgets.prunn.wtcc_2011.qtime.QualTimeWidget;

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
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class QualifInfoWidget extends Widget
{
    
    
    private DrawnString dsPos = null;
    private DrawnString dsName = null;
    private TextureImage2D texManufacturer = null;
    private final ImagePropertyWithTexture imgBMW = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/bmw.png" );
    private final ImagePropertyWithTexture imgName = new ImagePropertyWithTexture( "imgName", "prunn/WTCC/onboard.png" );
    
    private final FontProperty posFont = new FontProperty("positionFont", PrunnWidgetSet_wtcc_2011.WTCC_2011_POS_FONT_NAME);
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    private final ColorProperty fontColor1 = new ColorProperty( "fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME );
    private final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    private IntProperty knockout = new IntProperty("Knockout position", 10);
    protected final ColorProperty KnockoutFontColor = new ColorProperty("Knockout Font Color", "KnockoutFontColor");
    private BooleanProperty uppercasename = new BooleanProperty("uppercase name",true); 
    private final FloatValue sessionTime = new FloatValue(-1F, 0.1F);
    
    private final DelayProperty visibleTime;
    private long visibleEnd;
    private IntValue cveh = new IntValue();
    private BoolValue cpit = new BoolValue();
    StandardTLCGenerator gen = new StandardTLCGenerator();
    private StringValue team = new StringValue();
    private StringValue name = new StringValue();
    private StringValue pos = new StringValue();
    private StringValue gap = new StringValue();
    private StringValue time = new StringValue();
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    private int NumOfPNG = 0;
    private String[] listPNG;
    
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
        
        team.update( "" );
        name.update( "" ); 
        pos.update( "" );
        gap.update( "" ); 
        time.update( "" ); 
        
        int rowHeight = height / 2;
        int fh = TextureImage2D.getStringHeight( "0", wtcc_2011_Font );
        //int fhPos = TextureImage2D.getStringHeight( "0", posFont );
        
        imgName.updateSize(width, height, isEditorMode );
        
        int top1 = ( rowHeight - fh ) / 2;
        int top2 = rowHeight + ( rowHeight - fh ) / 2;
        dsPos = drawnStringFactory.newDrawnString( "dsPos", width*13/100, top2 + fontyoffset.getValue(), Alignment.CENTER, false, posFont.getFont(), isFontAntiAliased(), fontColor1.getColor() );
        dsName = drawnStringFactory.newDrawnString( "dsName", width*23/100, top1 + fontyoffset.getValue(), Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor() );
        
        
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
    protected Boolean updateVisibility(LiveGameData gameData, boolean isEditorMode)
    {
        
        
        super.updateVisibility(gameData, isEditorMode);
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        cveh.update(scoringInfo.getViewedVehicleScoringInfo().getDriverId());
        cpit.update(scoringInfo.getViewedVehicleScoringInfo().isInPits());
        
        //if(QualTimeWidget.visible())
        //    return false;
        
        if((cveh.hasChanged() || cpit.hasChanged()) && !isEditorMode)
        {
            forceCompleteRedraw(true);
            visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            return true;
        }
        
        if(scoringInfo.getSessionNanos() < visibleEnd || cpit.getValue())
            return true;
        
        
        return false;	
    }
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        texture.clear( imgName.getTexture(), offsetX, offsetY, false, null );
        
        VehicleScoringInfo vsi1 = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        String team;
        
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
                imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                texManufacturer = imgBMW.getImage().getScaledTextureImage( width*13/100, height*43/100, texManufacturer, isEditorMode );
                texture.drawImage( texManufacturer, offsetX + width*24/100, offsetY + height*54/100, true, null );
                break;
            }
        }
        
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
    	sessionTime.update(scoringInfo.getSessionTime());
    	
    	if ( needsCompleteRedraw )
        {
    	    VehicleScoringInfo currentcarinfos = gameData.getScoringInfo().getViewedVehicleScoringInfo();
            
        	name.update( gen.ShortNameWTCC(currentcarinfos.getDriverName()) );
            if(currentcarinfos.getVehicleInfo() != null)
                pos.update( NumberUtil.formatFloat( currentcarinfos.getVehicleInfo().getCarNumber(), 0, true));
            else
                pos.update( NumberUtil.formatFloat( currentcarinfos.getDriverId(), 0, true));
            
            if(currentcarinfos.getVehicleInfo() != null)
                team.update( gen.generateShortTeamNames( currentcarinfos.getVehicleInfo().getFullTeamName(), gameData.getFileSystem().getConfigFolder() ));
            else
                team.update( currentcarinfos.getVehicleClass()); 
                
        	
            
            if( currentcarinfos.getFastestLaptime() != null && currentcarinfos.getFastestLaptime().getLapTime() > 0 )
            {
                if(currentcarinfos.getPlace( false ) > 1)
                { 
                    time.update( TimingUtil.getTimeAsLaptimeString(currentcarinfos.getBestLapTime() ));
                    gap.update( "+ " +  TimingUtil.getTimeAsLaptimeString( currentcarinfos.getBestLapTime() - gameData.getScoringInfo().getLeadersVehicleScoringInfo().getBestLapTime() ));
                }
                else
                {
                    time.update("");
                    gap.update( TimingUtil.getTimeAsLaptimeString(currentcarinfos.getBestLapTime()));
                }
                    
            }
            else
            {
                time.update("");
                gap.update("");
            }
            //if( currentcarinfos.getFastestLaptime() != null && currentcarinfos.getFastestLaptime().getLapTime() > 0 )
                dsPos.draw( offsetX, offsetY, pos.getValue(), texture );
            //if(( clock.c() && name.hasChanged()) || isEditorMode )
            if( uppercasename.getValue() )
                dsName.draw( offsetX, offsetY, name.getValue().toUpperCase(), texture );
            else    
                dsName.draw( offsetX, offsetY, name.getValue(), texture );
            //if(( clock.c() && team.hasChanged()) || isEditorMode )
               // dsTeam.draw( offsetX, offsetY, team.getValue(), texture );
            //if(( clock.c() && time.hasChanged()) || isEditorMode ) 
               // dsTime.draw( offsetX, offsetY, time.getValue(), texture);
            //if(( clock.c() && gap.hasChanged()) || isEditorMode )
               // dsGap.draw( offsetX, offsetY, gap.getValue(), texture );
        }
         
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( posFont, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( KnockoutFontColor, "" );
        writer.writeProperty(visibleTime, "");
        writer.writeProperty( knockout, "" );
        writer.writeProperty( fontyoffset, "" );
        writer.writeProperty( uppercasename, "" );
        
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( posFont ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( KnockoutFontColor ) );
        else if( loader.loadProperty(visibleTime));
        else if ( loader.loadProperty( knockout ) );
        else if ( loader.loadProperty( fontyoffset ) );
        else if ( loader.loadProperty( uppercasename ) );
        
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Colors" );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( posFont );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty( KnockoutFontColor );
        propsCont.addProperty(visibleTime);
        propsCont.addProperty( knockout );
        propsCont.addProperty( fontyoffset );
        propsCont.addProperty( uppercasename );
        
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
    
    public QualifInfoWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 40.0f, 7.0f );
        visibleTime = new DelayProperty("visibleTime", net.ctdp.rfdynhud.properties.DelayProperty.DisplayUnits.SECONDS, 6);
        visibleEnd = 0x8000000000000000L;
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
    }
    
}
