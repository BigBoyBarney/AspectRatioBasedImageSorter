package com.barney.image
import java.io.*

/* Original class written by Jaimon Mathew (http://blog.jaimon.co.uk/simpleimageinfo/SimpleImageInfo.java.html
and https://jaimonmathew.wordpress.com/2011/01/29/simpleimageinfo/) rewritten in Kotlin.
* */

class ImageInfo(file: File?) {
    var height = 0
    var width = 0
    var mimeType: String? = null

    init {
        val stream: InputStream = FileInputStream(file!!)
        stream.use { inputStream ->
            processStream(inputStream)
        }
    }


    @Throws(IOException::class)
    private fun processStream(IS: InputStream) {
        val c1 = IS.read()
        val c2 = IS.read()
        var c3 = IS.read()
        mimeType = null
        height = -1
        width = height
        if (c1 == 'G'.code && c2 == 'I'.code && c3 == 'F'.code) { // GIF
            IS.skip(3)
            width = readInt(IS, 2, false)
            height = readInt(IS, 2, false)
            mimeType = "image/gif"
        } else if (c1 == 0xFF && c2 == 0xD8) { // JPG
            while (c3 == 255) {
                val marker = IS.read()
                val len = readInt(IS, 2, true)
                if (marker == 192 || marker == 193 || marker == 194) {
                    IS.skip(1)
                    height = readInt(IS, 2, true)
                    width = readInt(IS, 2, true)
                    mimeType = "image/jpeg"
                    break
                }
                IS.skip((len - 2).toLong())
                c3 = IS.read()
            }
        } else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
            IS.skip(15)
            width = readInt(IS, 2, true)
            IS.skip(2)
            height = readInt(IS, 2, true)
            mimeType = "image/png"
        } else if (c1 == 66 && c2 == 77) { // BMP
            IS.skip(15)
            width = readInt(IS, 2, false)
            IS.skip(2)
            height = readInt(IS, 2, false)
            mimeType = "image/bmp"
        } else {
            val c4 = IS.read()
            if ((c1 == 'M'.code && c2 == 'M'.code && c3 == 0 && c4 == 42) || (c1 == 'I'.code && c2 == 'I'.code && c3 == 42 && c4 == 0)) { //TIFF
                val bigEndian = c1 == 'M'.code
                val ifd: Int = readInt(IS, 4, bigEndian)
                IS.skip((ifd - 8).toLong())
                val entries: Int = readInt(IS, 2, bigEndian)
                for (i in 1..entries) {
                    val tag = readInt(IS, 2, bigEndian)
                    val fieldType = readInt(IS, 2, bigEndian)
                    readInt(IS, 4, bigEndian).toLong()
                    var valOffset: Int
                    if (fieldType == 3 || fieldType == 8) {
                        valOffset = readInt(IS, 2, bigEndian)
                        IS.skip(2)
                    } else {
                        valOffset = readInt(IS, 4, bigEndian)
                    }
                    if (tag == 256) {
                        width = valOffset
                    } else if (tag == 257) {
                        height = valOffset
                    }
                    if (width != -1 && height != -1) {
                        mimeType = "image/tiff"
                        break
                    }
                }
            }
        }
        if (mimeType == null) {
            throw IOException("Unsupported image type")
        }
    }

    @Throws(IOException::class)
    private fun readInt(`is`: InputStream, noOfBytes: Int, bigEndian: Boolean): Int {
        var ret = 0
        var sv = if (bigEndian) (noOfBytes - 1) * 8 else 0
        val cnt = if (bigEndian) -8 else 8
        for (i in 0 until noOfBytes) {
            ret = ret or (`is`.read() shl sv)
            sv += cnt
        }
        return ret
    }

    override fun toString(): String {
        return "MIME Type : $mimeType\t Width : $width\t Height : $height"
    }
}