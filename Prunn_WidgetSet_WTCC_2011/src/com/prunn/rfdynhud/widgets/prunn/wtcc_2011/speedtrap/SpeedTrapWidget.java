package com.prunn.rfdynhud.widgets.prunn.wtcc_2011.speedtrap;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.prunn.rfdynhud.widgets.prunn._util.PrunnWidgetSet_wtcc_2011;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
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
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * @author Prunn
 * copyright@Prunn2011
 * 
 */


public class SpeedTrapWidget extends Widget
{
    
    private DrawnString dsName = null;
    private DrawnString dsCurSpeed = null;
    //private DrawnString dsSlabel = null;
    private TextureImage2D texManufacturer = null;
    private final ImagePropertyWithTexture imgBMW = new ImagePropertyWithTexture( "imgTime", "prunn/WTCC/bmw.png" );
    private final ImagePropertyWithTexture imgTrap = new ImagePropertyWithTexture( "imgName", "prunn/WTCC/speedtrap.png" );
    
    protected final FontProperty wtcc_2011_Font = new FontProperty("Main Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME);
    protected final FontProperty wtcc_2011_SpeedFont = new FontProperty("Speed Font", PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_SPEEDTRAP);
    
    protected final ColorProperty fontColor1 = new ColorProperty("fontColor1", PrunnWidgetSet_wtcc_2011.FONT_COLOR1_NAME);
    protected final ColorProperty fontColor2 = new ColorProperty("fontColor2", PrunnWidgetSet_wtcc_2011.FONT_COLOR2_NAME);
    protected final ColorProperty fontColorTopSpeed = new ColorProperty("fontColor5", PrunnWidgetSet_wtcc_2011.FONT_COLOR_TOP_SPEED_NAME);
    private final BooleanProperty forceleader = new BooleanProperty("Force Leader", false);
    private IntProperty fontyoffset = new IntProperty("Y Font Offset", 0);
    
    FloatValue CurSpeed = new FloatValue(-1F, 0.001F);
    
    private FloatValue topspeedchanged = new FloatValue(-1F, 0.001F);
    private float speedtrap = 0;
    private float lastspeed = 0;
    private float topspeed = 0;
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
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int rowHeight = height / 2;
        int fh = TextureImage2D.getStringHeight( "0gyI", wtcc_2011_Font );
        int top1 = ( rowHeight - fh ) / 2 + fontyoffset.getValue();
        int top2 = rowHeight + ( rowHeight - fh ) / 2 + fontyoffset.getValue();
        
        imgTrap.updateSize( width, height, isEditorMode );
        
        if(!isEditorMode)
            speedtrap = GetSpeedTrapLogFile(gameData);
        else
            speedtrap = 0;

        //dsSlabel = drawnStringFactory.newDrawnString( "dsSlabel", width*17/100, top2, Alignment.LEFT, false, wtcc_2011_SpeedFont.getFont(), isFontAntiAliased(), fontColorTopSpeed.getColor());
        dsCurSpeed = drawnStringFactory.newDrawnString( "dsCurSpeed", width*90/100, top2, Alignment.RIGHT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor1.getColor());
        dsName = drawnStringFactory.newDrawnString( "dsCurSpeed", width*23/100, top1, Alignment.LEFT, false, wtcc_2011_Font.getFont(), isFontAntiAliased(), fontColor2.getColor());
        
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
        VehicleScoringInfo currentcarinfos = scoringInfo.getViewedVehicleScoringInfo();
        topspeedchanged.update( currentcarinfos.getTopspeed() );
        
        if(topspeedchanged.hasChanged() && currentcarinfos.getStintLength() > 1 && lastspeed >= CurSpeed.getValue() && !isEditorMode)
        {
            if(speedtrap <= currentcarinfos.getLapDistance() - 120 || speedtrap >= currentcarinfos.getLapDistance() + 120 || speedtrap == 0)
            {
                speedtrap = currentcarinfos.getLapDistance();
                try
                {
                    UpdateSpeedTrapLogFile(speedtrap, gameData);
                }
                catch ( IOException e )
                {
                    log(e);
                }
            }
        }
        
        //200 m range for listenning, visible 300m after the trap
        if(currentcarinfos.getLapDistance() >= speedtrap - 200 && currentcarinfos.getLapDistance() <= speedtrap + 300 && currentcarinfos.getCurrentLaptime() > 0 && ( speedtrap != 0 || isEditorMode))
        {
            CurSpeed.update(  currentcarinfos.getScalarVelocity() ) ;
            
            if(lastspeed > CurSpeed.getValue())
            {
                topspeed = lastspeed;
                return true;
            }
            else
            {
                lastspeed = CurSpeed.getValue();
                return false;
            }
            
        }
        else
            lastspeed = CurSpeed.getValue();
            
            
        return false;
         
    }
    
    protected void UpdateSpeedTrapLogFile(float trapvalue, LiveGameData data) throws IOException
    {
        
        Writer output = null;
        String text = NumberUtil.formatFloat( trapvalue, 0, false );
        File file = new File(data.getFileSystem().getCacheFolder() + "/data/speedtraps/" + data.getTrackInfo().getTrackName() + ".spt");
        output = new BufferedWriter(new FileWriter(file));
        output.write(text);
        output.close();     
                
    }
    protected float GetSpeedTrapLogFile(LiveGameData data)
    {
        float trapvalue = 0;
        
        try
        {
            File file = new File(data.getFileSystem().getCacheFolder() + "/data/speedtraps/" + data.getTrackInfo().getTrackName() + ".spt");
            BufferedReader br = new BufferedReader( new FileReader( file ) );
            
            trapvalue = Float.valueOf(br.readLine()).floatValue();
            
            br.close();
        }
        catch (Exception e)
        {
            trapvalue = 0;
        }
        
            
        return trapvalue;
        
        
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        
        texture.clear( imgTrap.getTexture(), offsetX, offsetY, false, null );
        
        
        VehicleScoringInfo vsi1 = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        String team;
        
        if(isEditorMode)
            team = "chevrolet";
        else if(vsi1.getVehicleInfo() != null)
            team = vsi1.getVehicleInfo().getManufacturer();
        else
            team = vsi1.getVehicleClass();
        
        
        for(int i=0; i < NumOfPNG; i++)
        {
            if(team.length() >= listPNG[i].length() && team.substring( 0, listPNG[i].length() ).toUpperCase().equals( listPNG[i].toUpperCase() )) 
            {
                imgBMW.setValue("prunn/WTCC/Manufacturer/" + listPNG[i] + ".png");
                texManufacturer = imgBMW.getImage().getScaledTextureImage( width*12/100, height*43/100, texManufacturer, isEditorMode );
                texture.drawImage( texManufacturer, offsetX + width*4/100, offsetY + height*53/100, true, null );
                break;
            }
        }
        
        
    }
    
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if(isEditorMode)
            topspeed = 325.5f;
        if ( needsCompleteRedraw || clock.c() )
        {
            //dsSlabel.draw( offsetX, offsetY, "TOP SPEED", texture );
            if(gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.METRIC)
                dsCurSpeed.draw( offsetX, offsetY, NumberUtil.formatFloat( topspeed ,1,false) + " Kph", texture );
            else
                dsCurSpeed.draw( offsetX, offsetY, NumberUtil.formatFloat( topspeed ,1,false) + " mph", texture );
            
            dsName.draw( offsetX, offsetY, PrunnWidgetSet_wtcc_2011.ShortNameWTCC( gameData.getScoringInfo().getViewedVehicleScoringInfo().getDriverName().toUpperCase() ), texture );
            
        }
         
    }
    
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        writer.writeProperty( wtcc_2011_Font, "" );
        writer.writeProperty( wtcc_2011_SpeedFont, "" );
        writer.writeProperty( fontColor1, "" );
        writer.writeProperty( fontColor2, "" );
        writer.writeProperty( fontColorTopSpeed, "" );
        writer.writeProperty( forceleader, "" );
        writer.writeProperty( fontyoffset, "" );
        
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        if ( loader.loadProperty( wtcc_2011_Font ) );
        else if ( loader.loadProperty( wtcc_2011_SpeedFont ) );
        else if ( loader.loadProperty( fontColor1 ) );
        else if ( loader.loadProperty( fontColor2 ) );
        else if ( loader.loadProperty( fontColorTopSpeed ) );
        else if ( loader.loadProperty( forceleader ) );
        else if ( loader.loadProperty( fontyoffset ) );
        
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Colors" );
        propsCont.addProperty( wtcc_2011_Font );
        propsCont.addProperty( wtcc_2011_SpeedFont );
        propsCont.addProperty( fontColor1 );
        propsCont.addProperty( fontColor2 );
        propsCont.addProperty( fontColorTopSpeed );
        propsCont.addProperty( forceleader );

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
    
    public SpeedTrapWidget()
    {
        super( PrunnWidgetSet_wtcc_2011.INSTANCE, PrunnWidgetSet_wtcc_2011.WIDGET_PACKAGE_WTCC_2011, 17.0f, 9.3f );
        getBackgroundProperty().setColorValue( "#00000000" );
        getFontProperty().setFont( PrunnWidgetSet_wtcc_2011.WTCC_2011_FONT_NAME );
    }
    
}
