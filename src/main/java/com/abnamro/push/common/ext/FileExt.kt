package com.abnamro.push.common.ext

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO


fun File.toText() = this.readText(Charsets.UTF_8)

fun File.writeToImage(byteArray: ByteArray){
    val bis = ByteArrayInputStream(byteArray)
    val bImage2 = ImageIO.read(BufferedInputStream(bis))
    ImageIO.write(bImage2, "bmp", this );
}


