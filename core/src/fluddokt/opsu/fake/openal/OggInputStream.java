package fluddokt.opsu.fake.openal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import java.nio.ShortBuffer;

//import org.lwjgl.BufferUtils;
//import org.newdawn.slick.util.Log;
import fluddokt.opsu.fake.*;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * An input stream that can extract ogg data. This class is a bit of an experiment with continuations
 * so uses thread where possibly not required. It's just a test to see if continuations make sense in 
 * some cases.
 *
 * @author kevin
 */
public class OggInputStream extends AudioInputStream2 {
	/** The conversion buffer size */
	private int convsize = 4096 * 4;
	/** The buffer used to read OGG file */
	private short[] convbuffer = new short[convsize]; // take 8k out of the data segment, not the stack
	/** The stream we're reading the OGG file from */
	private InputStream input;
	/** The audio information from the OGG header */
	private Info oggInfo = new Info(); // struct that stores all the static vorbis bitstream settings
	/** True if we're at the end of the available data */
	private boolean endOfStream;

	/** The Vorbis SyncState used to decode the OGG */
	private SyncState syncState = new SyncState(); // sync and verify incoming physical bitstream
	/** The Vorbis Stream State used to decode the OGG */
	private StreamState streamState = new StreamState(); // take physical pages, weld into a logical stream of packets
	/** The current OGG page */
	private Page page = new Page(); // one Ogg bitstream page.  Vorbis packets are inside
	/** The current packet page */
	private Packet packet = new Packet(); // one raw packet of data for decode

	/** The comment read from the OGG file */
	private Comment comment = new Comment(); // struct that stores all the bitstream user comments
	/** The Vorbis DSP stat eused to decode the OGG */
	private DspState dspState = new DspState(); // central working state for the packet->PCM decoder
	/** The OGG block we're currently working with to convert PCM */
	private Block vorbisBlock = new Block(dspState); // local working space for packet->PCM decode
	
	/** Temporary scratch buffer */
	byte[] buffer;
	/** The number of bytes read */
	int bytes = 0;
	/** The true if we should be reading big endian */
	boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
	/** True if we're reached the end of the current bit stream */
	boolean endOfBitStream = true;
	/** True if we're initialise the OGG info block */
	boolean inited = false;
	
	/** The index into the byte array we currently read from */
	private int readIndex;
	/** The byte array store used to hold the data read from the ogg */
	private ShortBuffer pcmBuffer = BufferUtils.createShortBuffer(4096 * 500);
	/** The total number of bytes */
	private int total;
	
	/**
	 * Create a new stream to decode OGG data
	 * 
	 * @param input The input stream from which to read the OGG file
	 * @throws IOException Indicates a failure to read from the supplied stream
	 */
	public OggInputStream(InputStream input) throws IOException {
		this.input = input;
		total = input.available();
		
		init();
	}
	
	/**
	 * Get the number of bytes on the stream
	 * 
	 * @return The number of the bytes on the stream
	 */
	public int getLength() {
		return total;
	}
	
	/**
	 * @see org.newdawn.slick.openal.AudioInputStream2#getChannels()
	 */
	public int getChannels() {
		return oggInfo.channels;
	}
	
	/**
	 * @see org.newdawn.slick.openal.AudioInputStream2#getRate()
	 */
	public int getRate() {
		return oggInfo.rate;
	}
	
	/**
	 * Initialise the streams and thread involved in the streaming of OGG data
	 * 
	 * @throws IOException Indicates a failure to link up the streams
	 */
	private void init() throws IOException {
		initVorbis();
		readPCM();
	}
		
	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() {
		return endOfStream ? 0 : 1;
	}
	
	/**
	 * Initialise the vorbis decoding
	 */
	private void initVorbis() {
		syncState.init();
	}
	
	/**
	 * Get a page and packet from that page
	 *
	 * @return True if there was a page available
	 */
	private boolean getPageAndPacket() {
		// grab some data at the head of the stream.  We want the first page
		// (which is guaranteed to be small and only contain the Vorbis
		// stream initial header) We need the first page to get the stream
		// serialno.

		// submit a 4k block to libvorbis' Ogg layer
		int index = syncState.buffer(4096);
		
		buffer = syncState.data;
		if (buffer == null) {
			endOfStream = true;
			return false;
		}
		
		try {
			bytes = input.read(buffer, index, 4096);
		} catch (Exception e) {
			Log.error("Failure reading in vorbis");
			Log.error(e);
			endOfStream = true;
			return false;
		}
		syncState.wrote(bytes);

		// Get the first page.
		if (syncState.pageout(page) != 1) {
			// have we simply run out of data?  If so, we're done.
			if (bytes < 4096)
				return false;

			// error case.  Must not be Vorbis data
			Log.warn("Input does not appear to be an Ogg bitstream.");
			endOfStream = true;
			return false;
		}

		// Get the serial number and set up the rest of decode.
		// serialno first; use it to set up a logical stream
		streamState.init(page.serialno());

		// extract the initial header from the first page and verify that the
		// Ogg bitstream is in fact Vorbis data

		// I handle the initial header first instead of just having the code
		// read all three Vorbis headers at once because reading the initial
		// header is an easy way to identify a Vorbis bitstream and it's
		// useful to see that functionality seperated out.

		oggInfo.init();
		comment.init();
		if (streamState.pagein(page) < 0) {
			// error; stream version mismatch perhaps
			Log.error("Error reading first page of Ogg bitstream data.");
			endOfStream = true;
			return false;
		}

		if (streamState.packetout(packet) != 1) {
			// no page? must not be vorbis
			Log.error("Error reading initial header packet.");
			endOfStream = true;
			return false;
		}

		if (oggInfo.synthesis_headerin(comment, packet) < 0) {
			// error case; not a vorbis header
			Log.error("This Ogg bitstream does not contain Vorbis audio data.");
			endOfStream = true;
			return false;
		}

		// At this point, we're sure we're Vorbis.  We've set up the logical
		// (Ogg) bitstream decoder.  Get the comment and codebook headers and
		// set up the Vorbis decoder

		// The next two packets in order are the comment and codebook headers.
		// They're likely large and may span multiple pages.  Thus we reead
		// and submit data until we get our two pacakets, watching that no
		// pages are missing.  If a page is missing, error out; losing a
		// header page is the only place where missing data is fatal. */

		int i = 0;
		while (i < 2) {
			while (i < 2) {

				int result = syncState.pageout(page);
				if (result == 0)
					break; // Need more data
				// Don't complain about missing or corrupt data yet.  We'll
				// catch it at the packet output phase

				if (result == 1) {
					streamState.pagein(page); // we can ignore any errors here
					// as they'll also become apparent
					// at packetout
					while (i < 2) {
						result = streamState.packetout(packet);
						if (result == 0)
							break;
						if (result == -1) {
							// Uh oh; data at some point was corrupted or missing!
							// We can't tolerate that in a header.  Die.
							Log.error("Corrupt secondary header.  Exiting.");
							endOfStream = true;
							return false;
						}
						
						oggInfo.synthesis_headerin(comment, packet);
						i++;
					}
				}
			}
			// no harm in not checking before adding more
			index = syncState.buffer(4096);
			buffer = syncState.data;
			try {
				bytes = input.read(buffer, index, 4096);
			} catch (Exception e) {
				Log.error("Failed to read Vorbis: ");
				Log.error(e);
				endOfStream = true;
				return false;
			}
			if (bytes == 0 && i < 2) {
				Log.error("End of file before finding all Vorbis headers!");
				endOfStream = true;
				return false;
			}
			syncState.wrote(bytes);
		}

		convsize = 4096 / oggInfo.channels;

		// OK, got and parsed all three headers. Initialize the Vorbis
		//  packet->PCM decoder.
		dspState.synthesis_init(oggInfo); // central decode state
		vorbisBlock.init(dspState); // local state for most of the decode
		// so multiple block decodes can
		// proceed in parallel.  We could init
		// multiple vorbis_block structures
		// for vd here
		
		return true;
	}
	
	/**
	 * Decode the OGG file as shown in the jogg/jorbis examples
	 * 
	 * @throws IOException Indicates a failure to read from the supplied stream
	 */
	private void readPCM() throws IOException {
		boolean wrote = false;
		
		while (true) { // we repeat if the bitstream is chained
			if (endOfBitStream) {
				if (!getPageAndPacket()) {
					break;
				}
				endOfBitStream = false;
			}

			if (!inited) {
				inited = true;
				return;
			}
			
			float[][][] _pcm = new float[1][][];
			int[] _index = new int[oggInfo.channels];
			// The rest is just a straight decode loop until end of stream
			while (!endOfBitStream) {
				while (!endOfBitStream) {
					int result = syncState.pageout(page);
					
					if (result == 0) {
						break; // need more data
					}
					
					if (result == -1) { // missing or corrupt data at this page position
						Log.error("Corrupt or missing data in bitstream; continuing...");
					} else {
						streamState.pagein(page); // can safely ignore errors at
						// this point
						while (true) {
							result = streamState.packetout(packet);

							if (result == 0)
								break; // need more data
							if (result == -1) { // missing or corrupt data at this page position
								// no reason to complain; already complained above
							} else {
								// we have a packet.  Decode it
								int samples;
								if (vorbisBlock.synthesis(packet) == 0) { // test for success!
									dspState.synthesis_blockin(vorbisBlock);
								}

								// **pcm is a multichannel float vector.  In stereo, for
								// example, pcm[0] is left, and pcm[1] is right.  samples is
								// the size of each channel.  Convert the float values
								// (-1.<=range<=1.) to whatever PCM format and write it out

								while ((samples = dspState.synthesis_pcmout(_pcm,
										_index)) > 0) {
									float[][] pcm = _pcm[0];
									//boolean clipflag = false;
									int bout = (samples < convsize ? samples
											: convsize);

									// convert floats to 16 bit signed ints (host order) and
									// interleave
									for (int i = 0; i < oggInfo.channels; i++) {
										int ptr = i * 1;
										//int ptr=i;
										int mono = _index[i];
										for (int j = 0; j < bout; j++) {
											int val = (int) (pcm[i][mono + j] * 32767.);
											// might as well guard against clipping
											if (val > 32767) {
												val = 32767;
											}
											if (val < -32768) {
												val = -32768;
											}
											if (val < 0)
												val = val | 0x8000;
											convbuffer[ptr] = (short) (val);
											/*
											if (bigEndian) {
												convbuffer[ptr] = (byte) (val >>> 8);
												convbuffer[ptr + 1] = (byte) (val);
											} else {
												convbuffer[ptr] = (byte) (val);
												convbuffer[ptr + 1] = (byte) (val >>> 8);
											}*/
											ptr += 1 * (oggInfo.channels);
										}
									}

									int bytesToWrite = 1 * oggInfo.channels * bout;
									if (bytesToWrite >= pcmBuffer.remaining()) {
										Log.warn("Read block from OGG that was too big to be buffered: " + bytesToWrite);
									} else {
										pcmBuffer.put(convbuffer, 0, bytesToWrite);
									}
									
									wrote = true;
									dspState.synthesis_read(bout); // tell libvorbis how
									// many samples we
									// actually consumed
								}
							}
						}
						if (page.eos() != 0) {
							endOfBitStream = true;
						} 
						
						if ((!endOfBitStream) && (wrote)) {
							return;
						}
					}
				}

				if (!endOfBitStream) {
					bytes = 0;
					int index = syncState.buffer(4096);
					if (index >= 0) {
						buffer = syncState.data;
						try {
							bytes = input.read(buffer, index, 4096);
						} catch (Exception e) {
							Log.error("Failure during vorbis decoding");
							Log.error(e);
							endOfStream = true;
							return;
						}
					} else {
						bytes = 0;
					}
					syncState.wrote(bytes);
					if (bytes == 0) {
						endOfBitStream = true;
					}
				}
			}

			// clean up this logical bitstream; before exit we see if we're
			// followed by another [chained]
			streamState.clear();

			// ogg_page and ogg_packet structs always point to storage in
			// libvorbis.  They're never freed or manipulated directly

			vorbisBlock.clear();
			dspState.clear();
			oggInfo.clear(); // must be called last
		}

		// OK, clean up the framer
		syncState.clear();
		endOfStream = true;
	}
	
	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if (readIndex >= pcmBuffer.position()) {
			pcmBuffer.clear();
			readPCM();
			readIndex = 0;
		}
		if (readIndex >= pcmBuffer.position()) {
			return -1;
		}

		int value = pcmBuffer.get(readIndex);
		readIndex++;
		
		return value & 0xffff;
	}

	/**
	 * @see org.newdawn.slick.openal.AudioInputStream2#atEnd()
	 */
	public boolean atEnd() {
		return endOfStream && (readIndex >= pcmBuffer.position());
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		input.close();
	}

}
