package com.Ostermiller.util;

import java.io.*;

/**
 * A StraightStreamReader is a bridge from byte streams to character streams: It reads bytes
 * and translates them into characters without using a character encoding.  The characters
 * that a StraightStreamReader returns may not be valid unicode characters but they are
 * guaranteed to be in the 0x00 to 0xFF range.
 * <P>
 * Most of the time you want to do character encoding translation when translating bytes to
 * characters.  If you are planning on displaying the text, you should always do this and should
 * use an InputStreamReader for the purpose.  Sometimes it is useful to treat characters as bytes
 * with some extra bits.  In these cases you would want to use a StraightStreamReader.
 */
public class StraightStreamReader extends Reader{

    /**
     * The input stream from which all methods in this class read.
     */
    private InputStream in;

    /**
     * Create a StraightStreamReader from an InputStream
     *
     * @param in InputStream to wrap a Reader around.
	 */
	StraightStreamReader(InputStream in) {
		this.in = in;
	}

    /**
     * Close the stream.
     *
     * @throws IOException If an I/O error occurs
     */
	public void close() throws IOException {
		in.close();
	}

    /**
     * Mark the present position in the stream. Subsequent calls to reset() 
	 * will attempt to reposition the stream to this point. Not all 
	 * character-input streams support the mark() operation.
     *
     * @param readAheadLimit Limit on the number of characters that may be read 
	 *    while still preserving the mark. After reading this many characters, 
	 *    attempting to reset the stream may fail.
     * @throws IOException If the stream does not support mark(), or if some other I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
    	in.mark(readAheadLimit);
    }

    /**
     * Tell whether this stream supports the mark() operation.
     *
	 * @return true if and only if this stream supports the mark operation.
     */
  	public boolean markSupported(){
  		return in.markSupported();
  	} 

    /**
	 * Read a single character. This method will block until a character is available, an 
	 * I/O error occurs, or the end of the stream is reached.
     * 
	 * @return The character read, as an integer in the range 0 to 256 (0x00-0xff), or -1 if 
	 *    the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
     */
  	public int read() throws IOException { 
        return in.read();
  	}

    /**
	 * Read characters into an array. This method will block until some input is available,
	 * an I/O error occurs, or the end of the stream is reached.
     *
	 * @param cbuf Destination buffer
     * @return The number of bytes read, or -1 if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
     */
  	public int read(char[] cbuf) throws IOException {
  		return read(cbuf, 0, cbuf.length);
  	}
    
    /**
	 * Read characters into an array. This method will block until some input is available,
	 * an I/O error occurs, or the end of the stream is reached.
     *
	 * @param cbuf Destination buffer
	 * @param off Offset at which to start storing characters
	 * @param len Maximum number of characters to read
	 * @return The number of bytes read, or -1 if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
     */
  	public int read(char[] cbuf, int off, int len) throws IOException {
		byte[] buffer = new byte[cbuf.length];
        int length = in.read(buffer, off, len);
        for (int i=0; i<length; i++){
            cbuf[off+i] = (char)(0xFF & buffer[off+i]);
		}
		return length;
   	}

  	/**
	 * Tell whether this stream is ready to be read.
     *
	 * @return True if the next read() is guaranteed not to block for input, false otherwise.
     *    Note that returning false does not guarantee that the next read will block.
	 * @throws IOException If an I/O error occurs
     */
  	public boolean ready() throws IOException {
  		return (in.available() > 0);
  	}

  	/**
	 * Reset the stream. If the stream has been marked, then attempt to reposition it at the mark. 
	 * If the stream has not been marked, then attempt to reset it in some way appropriate to the 
	 * particular stream, for example by repositioning it to its starting point. Not all 
	 * character-input streams support the reset() operation, and some support reset() 
	 * without supporting mark().
     *
	 * @throws IOException If the stream has not been marked, or if the mark has been invalidated, 
	 *    or if the stream does not support reset(), or if some other I/O error occurs
     */
  	public void reset() throws IOException {
  		in.reset();
  	}

  	/**
	 * Skip characters. This method will block until some characters are available, 
	 * an I/O error occurs, or the end of the stream is reached.
	 *
	 * @param n The number of characters to skip
	 * @return The number of characters actually skipped
	 * @throws IllegalArgumentException If n is negative
     * @throws IOException If an I/O error occurs
     */
  	public long skip(long n) throws IOException {
        return in.skip(n);
  	}

    /**
     * Regression test for this class.  If this class is working, this should
     * run and print no errors.
     * <P>
     * This method creates a tempory file in the working directory called "test.txt".
     * This file should not exist before hand, and the program should have create,
     * read, write, and delete access to this file.
     *
     * @param args command line arguments (ignored)
     */
    private static void main(String[] args){
        try {
            File f = new File("test.txt");
            if (f.exists()){
                throw new IOException(f + " already exists.  I don't want to overwrite it.");
            }
            StraightStreamReader in;
            char[] cbuf = new char[0x100];
            int read;

            // write a file with all possible values of bytes
			FileOutputStream out = new FileOutputStream(f);
            for (int i=0x00; i<0x100; i++){
                out.write(i);
            }
            out.close();

            // read it back using the read single character method
            in = new StraightStreamReader(new FileInputStream(f));
            for (int i=0x00; i<0x100; i++){
                read = in.read();
                if (read != i){
                	System.err.println("Error: " + i + " read as " + read);
                }
            }
            in.close();

            // read it back using buffer read method.
            in = new StraightStreamReader(new FileInputStream(f));
            int totRead = 0;
            while ((read = in.read(cbuf, totRead, cbuf.length-totRead)) > 0 && read <= cbuf.length){
                totRead += read;
			}
            if (totRead != 0x100){
                System.err.println("Not enough read. Bytes read: " + Integer.toHexString(totRead));
            }
            for (int i=0x00; i<totRead; i++){
               if (cbuf[i] != i){
                	System.err.println("Error: " + i + " read as " + cbuf[i]);
                }
            }
            in.close();
            f.delete();
        } catch (IOException x){
            System.err.println(x.getMessage());
		}
	}
}