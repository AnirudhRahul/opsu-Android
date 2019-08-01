package fluddokt.opsu.fake;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Bitmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Face;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.GlyphMetrics;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.GlyphSlot;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Library;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.SizeMetrics;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntMap;

import java.util.LinkedList;

public class DynamicFreeTypeFont {
	static int PAD = 1;
	FileHandle handle;
	Face face;
	Face backupface;
	Font fontParam;
	Font backParam;
	int ascent, descent, height;
	boolean useKerning = false;
	
	int thiscnt = 0;
	static int cnt = 0;
	
	public DynamicFreeTypeFont(FileHandle font, Font fontParam) {
		this.fontParam = fontParam;
		Library library = FreeType.initFreeType();
		if (library == null)
			throw new GdxRuntimeException("Couldn't initialize FreeType");
		face = library.newFace(font, 0);
		if (face == null)
			throw new GdxRuntimeException("Couldn't create face for font '"
					+ font + "'");

		if (!face.setPixelSizes(0, (int) Math.round(fontParam.size))) {
			throw new GdxRuntimeException("Couldn't set size for font '" + font
					+ "'");
		}

		SizeMetrics sizes = face.getSize().getMetrics();
		ascent = FreeType.toInt(sizes.getAscender());
		descent = FreeType.toInt(sizes.getDescender());
		height = FreeType.toInt(sizes.getHeight());
		// System.out.println("Face flag "+face.getFaceFlags());
//		useKerning = FreeType.hasKerning(face);

		thiscnt = cnt++;
	}

	// ArrayList<Texture> pages = new ArrayList<Texture>();
	//HashMap<Character, CharInfo> charmap = new HashMap<Character, CharInfo>();
	IntMap<CharInfo> charmap = new IntMap<CharInfo>();
	IntFloatMap charwidth = new IntFloatMap();
	
	Pixmap curPixmap;
	Texture curTexture;
	
	LinkedList<Pixmap> pixmapList = new LinkedList<>();
	LinkedList<Texture> textureList = new LinkedList<>();
	

	class CharInfo {
		TextureRegion region;
		public float horadvance;
		public float xbear;
		public float ybear;
		public float height;
		public int sBitmapTop;
		public int yoffset;
		public int sBitmapLeft;
		public int bitmapPitch;
		public int bitmapRows;
		public int bitmapWidth;
	}

	public void draw(SpriteBatch batch, String str, float x, float y) {
		x = (int)x;
		y = (int)y;
		//int prevchrIndex = 0;
		//CharInfo prevCharInfo = null;
		float ox = x;
		y+= ascent;
		int strlen = str.length();
		for (int i = 0; i < strlen; i++) {
			char thischr = str.charAt(i);
			if (thischr == '\n') {
				y += getLineHeight();
				x = ox;
			} else {
				/*if (useKerning) {
					//int thisChrIndex = FreeType.getCharIndex(face, thischr);
					float spacing = 0;//to26p6float(FreeType.getKerning(face, prevchrIndex, thisChrIndex,;
							//FreeType.FT_KERNING_DEFAULT));
					//prevchrIndex = thisChrIndex;
					
					//OpenType kerning via the 'GPOS' table is not supported! You need a higher-level library like HarfBuzz, Pango, or ICU, 
					//System.out.println(spacing+" "+thischr);
					if(spacing==0 && prevCharInfo != null)
						spacing += prevCharInfo.horadvance;
					x += spacing;
				}*/
				
				CharInfo ci = getCharInfo(thischr);
				TextureRegion tr = ci.region;
				batch.draw(tr, x + ci.sBitmapLeft// xoffset
				, y - ci.sBitmapTop);// ci.yoffset);//-
														// tr.getRegionHeight() +
														// ci.ybear );//-
				//if (!useKerning) {
					x += ci.horadvance;
				//}
				//prevCharInfo = ci;
			}
		}
	}

	private float to26p6float(int n) {
		return n / (float) (1 << 6);
	}

	private CharInfo getCharInfo(char charAt) {
		CharInfo ci = charmap.get(charAt);
		if (ci == null) {
			ci = addChar(charAt);
			charmap.put(charAt, ci);
		}
		return ci;
	}

	int x, y, maxHeight;

	public CharInfo addChar(char c) {
		Face tface;
		Font tparam;
		if (charExist(face, c)) {
			tface = face;
			tparam = fontParam;
		} else {
			tface = backupface;
			tparam = backParam;
		}
		tface.loadChar( c,
		// FreeType.FT_LOAD_RENDER
		// FreeType.FT_LOAD_DEFAULT
				fontParam.size < 16 ? FreeType.FT_LOAD_DEFAULT : 
							FreeType.FT_LOAD_NO_HINTING
							|FreeType.FT_LOAD_NO_BITMAP
		// FreeType.FT_LOAD_NO_AUTOHINT
		);// FT_LOAD_MONOCHROME FT_RENDER_MODE_LIGHT
		GlyphSlot slot = tface.getGlyph();
		slot.renderGlyph(FreeType.FT_RENDER_MODE_LIGHT);
		Bitmap bitmap = slot.getBitmap();

		// System.out.println("Pixel Mode "+bitmap.getPixelMode());
		Pixmap pixmap;
		if (bitmap.getPixelMode() == FreeType.FT_PIXEL_MODE_GRAY) {
			// pixmap = bitmap.getPixmap(Format.RGBA8888);
			// *
			pixmap = new Pixmap(bitmap.getWidth(), bitmap.getRows(),
					Format.RGBA8888);
			java.nio.ByteBuffer rbuf = bitmap.getBuffer();
			java.nio.ByteBuffer wbuf = pixmap.getPixels();

			for (int y = 0; y < pixmap.getHeight(); y++) {
				for (int x = 0; x < pixmap.getWidth(); x++) {
					byte curbyte = rbuf.get();
					wbuf.putInt((curbyte & 0xff) | 0xffffff00);
				}
			}// */

		} else if (bitmap.getPixelMode() == FreeType.FT_PIXEL_MODE_MONO) {
			pixmap = new Pixmap(bitmap.getWidth(), bitmap.getRows(),
					Format.RGBA8888);
			java.nio.ByteBuffer rbuf = bitmap.getBuffer();
			java.nio.ByteBuffer wbuf = pixmap.getPixels();

			byte curbyte = rbuf.get();
			int bitAt = 0;
			for (int y = 0; y < pixmap.getHeight(); y++) {
				for (int x = 0; x < pixmap.getWidth(); x++) {
					if (((curbyte >> (7 - bitAt)) & 1) > 0) {
						wbuf.putInt(0xffffffff);
					} else {
						wbuf.putInt(0x00000000);
					}
					bitAt++;
					if (bitAt >= 8) {
						bitAt = 0;
						if (rbuf.hasRemaining())
							curbyte = rbuf.get();
					}
				}
				if (bitAt > 0) {
					bitAt = 0;
					if (rbuf.hasRemaining())
						curbyte = rbuf.get();
				}
			}
		} else {
			throw new GdxRuntimeException("Unknown Freetype pixel mode :"
					+ bitmap.getPixelMode());
		}
		
		int pixMapWidth = pixmap.getWidth();
		if((fontParam.style&Font.BOLD) > 0){
		//	pixMapWidth+=1;
		}
		
		
		// create a new page
		if (curPixmap == null || y + pixmap.getHeight() > curPixmap.getHeight()) {
			x = 0;
			y = 0;
			maxHeight = 0;
			curPixmap = new Pixmap(1024, 1024, Format.RGBA8888);
			curTexture = new Texture(new PixmapTextureData(curPixmap, null,
					false, false, true));
			pixmapList.add(curPixmap);
			textureList.add(curTexture);
			curPixmap.setColor(0);
			curPixmap.fill();
		}
		
		// cant fit width, go to next line
		if (x + pixMapWidth > curPixmap.getWidth()) {
			x = 0;
			y += maxHeight + PAD;
			maxHeight = 0;
		}
		// find the max Height of the this line
		if (pixmap.getHeight() > maxHeight) {
			maxHeight = pixmap.getHeight();
		}

		curPixmap.setBlending(Blending.None);
		curPixmap.drawPixmap(pixmap, x, y);
		if((tparam.style&Font.BOLD) > 0){
			curPixmap.setBlending(Blending.SourceOver);
			curPixmap.drawPixmap(pixmap, x, y);
			curPixmap.drawPixmap(pixmap, x, y);
			curPixmap.setBlending(Blending.None);
		}
		

		curTexture.load(new PixmapTextureData(curPixmap, null, false, false,
				true));

		TextureRegion tr = new TextureRegion(curTexture, x, y,
				pixMapWidth, pixmap.getHeight());
		tr.flip(false, true);
		x += pixMapWidth + PAD;

		GlyphMetrics metrics = slot.getMetrics();
		CharInfo ci = new CharInfo();
		ci.region = tr;
		ci.horadvance = to26p6float(metrics.getHoriAdvance());// slot.getLinearHoriAdvance()>>16;
		ci.xbear = to26p6float(metrics.getHoriBearingX());
		ci.ybear = to26p6float(metrics.getHoriBearingY());
		ci.height = to26p6float(metrics.getHeight());
		ci.sBitmapTop = slot.getBitmapTop();
		ci.sBitmapLeft = slot.getBitmapLeft();
		ci.yoffset = slot.getBitmapTop() - pixmap.getHeight();
		ci.bitmapPitch = bitmap.getPitch();
		ci.bitmapRows = bitmap.getRows();
		ci.bitmapWidth = bitmap.getWidth();
		
		/*
		System.out.println("char: "+c+"hradv:"+ci.horadvance+" xbear:"+ci.xbear+" ybear"+ci.ybear+" height"+ci.height+
				" sBitmapTop"+ci.sBitmapTop+ " sBitmapLeft"+ci.sBitmapLeft
				+" bitmapPitch"+ci.bitmapPitch+" bitmapRows"+ci.bitmapRows+" bitmapWidth"+ci.bitmapWidth
				);*/
		pixmap.dispose();
		
		 
		return ci;
	}

	public int getHeight(String str) {
		/*float max = 0;
		for (int i = 0; i < str.length(); i++) {
			float t = getCharInfo(str.charAt(i)).height;
			if (t > max)
				max = t;
		}
		return (int) max;*/
		return getLineHeight();
	}

	public int getWidth(String str) {
		if (str == null)
			return 0;
		float len = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!charwidth.containsKey(c)) {
				charwidth.put(c, getCharWidth(c));
			}
			float t = charwidth.get(c, 0);
			len += t;
		}
		return (int) len;
	}

	private float getCharWidth(char c) {
		Face tface = charExist(face, c) ? face : backupface;
		tface.loadChar(c,
				fontParam.size < 16 ? FreeType.FT_LOAD_DEFAULT : 
									FreeType.FT_LOAD_NO_HINTING
				 |FreeType.FT_LOAD_NO_BITMAP
		);
		GlyphSlot slot = tface.getGlyph();
		GlyphMetrics metrics = slot.getMetrics();
		return to26p6float(metrics.getHoriAdvance());
	}
	
	private boolean charExist(Face face, char c) {
		return face.getCharIndex(c) != 0;
	}

	public int getLineHeight() {
		return ascent - descent ;

	}

	public void addBackupFace(Font backup) {
		this.backParam = backup;
		this.backupface = backup.dynFont.face;
	}

	public void destroy() {
		for(Pixmap p : pixmapList)
			p.dispose();
		pixmapList.clear();
		
		for(Texture t : textureList)
			t.dispose();
		textureList.clear();
	}
}
