package level;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import platform.BuzzSaw;
import platform.DisappearPlat;
import platform.Platform;
import platform.SawShooter;
import tile.TileMap;

import javax.swing.*;

import app.MeatBoyFrame;
import character.MeatBoy;
import character.BandageGirl;


public class MeatBoyLevel extends JPanel implements ActionListener{
	private Timer time;
	private MeatBoy player;
	private int mbxstart;
	private int mbystart;
	private BandageGirl destination;
	private int width;
	private int height;
	private int frame_width;
	private int frame_height;
	private ArrayList<Platform> platformList;
	private ArrayList<BuzzSaw> sawlist;
	private ArrayList<DisappearPlat> originaldplist;
	private ArrayList<DisappearPlat> dplist;
	private ArrayList<SawShooter > sslist;
	private int xscroll;
	private int yscroll;
	private BufferedImage entirebackground;
	private BufferedImage subbackground;
	private TileMap tmap;
	private boolean finished;
	private int deathCounter;
	
	public MeatBoyLevel(MeatBoyFrame frame)   {
		deathCounter=0;
		frame_height=frame.getHeight()-40;
 		frame_width=frame.getWidth();
		String src = "resources/factory2.tmx";	//change this to try other levels
		tmap = new TileMap(new File(src));
		entirebackground = tmap.drawMap();
		destination = tmap.getBandageGirl();
		mbxstart = tmap.getXStart();
		mbystart = tmap.getYStart();
		subbackground=null;
		width=tmap.getNumCols()*TileMap.TILE_SIZE;
		height=tmap.getNumRows()*TileMap.TILE_SIZE;
		
		platformList=tmap.getPlatforms();
		sawlist=tmap.getSaws();
		originaldplist=new ArrayList<DisappearPlat>(tmap.getDPs());
		dplist=new ArrayList<DisappearPlat>(tmap.getDPs());
		sslist=tmap.getSS(); //ss means sawshooter
		player = new MeatBoy(frame,this, mbxstart,mbystart);	
		destination = tmap.getBandageGirl();
		//start updating this level
		time=new Timer(40,this);
		time.start(); 

		
	}
	public void update(){
		if(!finished){
			if(player.getHitbox().intersects(destination.getHitbox())){
				finished=true;
			}
			player.move();
			xscroll=player.getXScroll()-frame_width/2;
			yscroll=player.getYScroll()-frame_height/2;
			if(xscroll<0||width<frame_width)
				xscroll=0;
			else if(xscroll>width-frame_width)
				xscroll=width-frame_width;
			if(yscroll<0||height<frame_height)
				yscroll=0;
			else if(yscroll>height-frame_height)
				yscroll=height-frame_height;
			player.setXScroll(xscroll);
			player.setYScroll(yscroll);
			for(int i=0;i<sslist.size();i++){
				sslist.get(i).setXScroll(xscroll);
				sslist.get(i).setYScroll(yscroll);
				if(sslist.get(i).refresh()){
					sawlist.add(new BuzzSaw(sslist.get(i).getX()+5,
							sslist.get(i).getY()+5,
							30,
							sslist.get(i).getXVel(),
							sslist.get(i).getYVel())
					);
				}
			}
			for(int i=0;i<sawlist.size();i++){

				sawlist.get(i).getAnimation().update();
				sawlist.get(i).setXScroll(xscroll);
				sawlist.get(i).setYScroll(yscroll);
				if(sawlist.get(i).isMoving()){
					sawlist.get(i).move();
					if(sawlist.get(i).getX()>width||sawlist.get(i).getY()>height||sawlist.get(i).getY()<0 ||sawlist.get(i).getX()<0){
						sawlist.remove(i);
					}
				}
			
			}
			for(int i=0;i<dplist.size();i++){
				dplist.get(i).setXScroll(xscroll);
				dplist.get(i).setYScroll(yscroll);
				if(dplist.get(i).isTouched()){
					dplist.get(i).getAnimation().update();
				}
			}
		}
	}
	public void paintComponent(Graphics g){
			super.paintComponent(g);
			subbackground = entirebackground.getSubimage(xscroll,yscroll, width<frame_width?width:frame_width, height<frame_height?height:frame_height)	;
			g.drawImage(subbackground, 0, 0,null);
			player.draw(g);
			for(int i=0;i<sslist.size();i++){
				if(sslist.get(i).getXScrolled()<frame_width&&sslist.get(i).getYScrolled()<frame_height){
					g.drawImage(sslist.get(i).getImage(),
							sslist.get(i).getXScrolled(), 
							sslist.get(i).getYScrolled(), 
							null
					);
				}
			}
			for(int i=0;i<sawlist.size();i++){
				if(sawlist.get(i).getX()<frame_width&&sawlist.get(i).getY()<frame_height){
					g.drawImage(
						sawlist.get(i).getAnimation().getImage(),
						sawlist.get(i).getX(),
						sawlist.get(i).getY(),
						null
					);
				}
			}	
			Iterator<DisappearPlat> iter = dplist.iterator();//using iterator so we can safely remove while traversing
			while(iter.hasNext()){
				DisappearPlat tmp = iter.next();
				if(tmp.getX()<frame_width&&tmp.getY()<frame_height&&!tmp.getAnimation().hasLooped()){
					g.drawImage(
							tmp.getAnimation().getImage(),
							tmp.getX(),
							tmp.getY(),
							null
						);
				}
				else if(tmp.getAnimation().hasLooped()){
					iter.remove();
				}
			}
			g.setColor(Color.red);
			g.drawString("Deaths: "+deathCounter,550,40);
	}
	public void incrementDeathCounter(){
		deathCounter++;
	}
	public ArrayList<Platform> getPlatforms(){
		return platformList;
	}
	public ArrayList<BuzzSaw> getSaws(){
		return sawlist;
	}	
	public ArrayList<DisappearPlat> getDPs(){
		return dplist;
	}
	public ArrayList<SawShooter> getSS(){
		return sslist;
	}
	public ArrayList<DisappearPlat> getOriginalDPs(){
		return new ArrayList<>(originaldplist);
	}	
	public void resetDPs(){
		for(DisappearPlat d:originaldplist){
			d.getAnimation().resetAnimation();
			d.resetTouched();
		}
		dplist=new ArrayList<DisappearPlat>(originaldplist);
	}
	public void actionPerformed(ActionEvent e){
		update();
		repaint();
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public int getXScroll(){
		return xscroll;
	}
	public int getYScroll(){
		return yscroll;
	}
}