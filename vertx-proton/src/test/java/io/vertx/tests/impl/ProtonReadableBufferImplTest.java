/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.tests.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

import io.vertx.proton.impl.ProtonReadableBufferImpl;
import io.vertx.proton.impl.ProtonWritableBufferImpl;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Tests for the ReadableBuffer wrapper that uses Netty ByteBuf underneath
 */
public class ProtonReadableBufferImplTest {

  @Test
  public void testWrapBuffer() {
    ByteBuf byteBuffer = Unpooled.buffer(100, 100);

    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(100, buffer.capacity());
    assertSame(byteBuffer, buffer.getBuffer());
    assertSame(buffer, buffer.reclaimRead());
  }

  @Test
  public void testArrayAccess() {
    ByteBuf byteBuffer = Unpooled.buffer(100, 100);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertTrue(buffer.hasArray());
    assertSame(buffer.array(), byteBuffer.array());
    assertEquals(buffer.arrayOffset(), byteBuffer.arrayOffset());
  }

  @Test
  public void testArrayAccessWhenNoArray() {
    ByteBuf byteBuffer = Unpooled.directBuffer();
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertFalse(buffer.hasArray());
  }

  @Test
  public void testByteBuffer() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    ByteBuffer nioBuffer = buffer.byteBuffer();
    assertEquals(data.length, nioBuffer.remaining());

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], nioBuffer.get());
    }
  }

  @Test
  public void testGet() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }

    assertFalse(buffer.hasRemaining());

    try {
      buffer.get();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetIndex() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get(i));
    }

    assertTrue(buffer.hasRemaining());
  }

  @Test
  public void testGetShort() {
    byte[] data = new byte[] { 0, 1 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(1, buffer.getShort());
    assertFalse(buffer.hasRemaining());

    try {
      buffer.getShort();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetInt() {
    byte[] data = new byte[] { 0, 0, 0, 1 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(1, buffer.getInt());
    assertFalse(buffer.hasRemaining());

    try {
      buffer.getInt();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetLong() {
    byte[] data = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(1, buffer.getLong());
    assertFalse(buffer.hasRemaining());

    try {
      buffer.getLong();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetFloat() {
    byte[] data = new byte[] { 0, 0, 0, 0 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(0, buffer.getFloat(), 0.0);
    assertFalse(buffer.hasRemaining());

    try {
      buffer.getFloat();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetDouble() {
    byte[] data = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(0, buffer.getDouble(), 0.0);
    assertFalse(buffer.hasRemaining());

    try {
      buffer.getDouble();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetBytes() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    byte[] target = new byte[data.length];

    buffer.get(target);
    assertFalse(buffer.hasRemaining());
    assertArrayEquals(data, target);

    try {
      buffer.get(target);
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetBytesIntInt() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    byte[] target = new byte[data.length];

    buffer.get(target, 0, target.length);
    assertFalse(buffer.hasRemaining());
    assertArrayEquals(data, target);

    try {
      buffer.get(target, 0, target.length);
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testGetBytesToWritableBuffer() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);
    ByteBuf targetBuffer = Unpooled.buffer(data.length, data.length);
    ProtonWritableBufferImpl target = new ProtonWritableBufferImpl(targetBuffer);

    buffer.get(target);
    assertFalse(buffer.hasRemaining());
    assertArrayEquals(targetBuffer.array(), data);
  }

  @Test
  public void testGetBytesToWritableBufferThatIsDirect() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.directBuffer(data.length, data.length);
    byteBuffer.writeBytes(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);
    ByteBuf targetBuffer = Unpooled.buffer(data.length, data.length);
    ProtonWritableBufferImpl target = new ProtonWritableBufferImpl(targetBuffer);

    buffer.get(target);
    assertFalse(buffer.hasRemaining());

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], target.getBuffer().readByte());
    }
  }

  @Test
  public void testDuplicate() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    ReadableBuffer duplicate = buffer.duplicate();

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], duplicate.get());
    }

    assertFalse(duplicate.hasRemaining());
  }

  @Test
  public void testSlice() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    ReadableBuffer slice = buffer.slice();

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], slice.get());
    }

    assertFalse(slice.hasRemaining());
  }

  @Test
  public void testLimit() {
    byte[] data = new byte[] { 1, 2 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(data.length, buffer.limit());
    buffer.limit(1);
    assertEquals(1, buffer.limit());
    assertEquals(1, buffer.get());
    assertFalse(buffer.hasRemaining());

    try {
      buffer.get();
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }
  }

  @Test
  public void testClear() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    byte[] target = new byte[data.length];

    buffer.get(target);
    assertFalse(buffer.hasRemaining());
    assertArrayEquals(data, target);

    try {
      buffer.get(target);
      fail("Should throw an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException ioe) {
    }

    buffer.clear();
    assertTrue(buffer.hasRemaining());
    assertEquals(data.length, buffer.remaining());
    buffer.get(target);
    assertFalse(buffer.hasRemaining());
    assertArrayEquals(data, target);
  }

  @Test
  public void testRewind() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }

    assertFalse(buffer.hasRemaining());
    buffer.rewind();
    assertTrue(buffer.hasRemaining());

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }
  }

  @Test
  public void testReset() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    buffer.mark();

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }

    assertFalse(buffer.hasRemaining());
    buffer.reset();
    assertTrue(buffer.hasRemaining());

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }
  }

  @Test
  public void testGetPosition() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(buffer.position(), 0);
    for (int i = 0; i < data.length; i++) {
      assertEquals(buffer.position(), i);
      assertEquals(data[i], buffer.get());
      assertEquals(buffer.position(), i + 1);
    }
  }

  @Test
  public void testSetPosition() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }

    assertFalse(buffer.hasRemaining());
    buffer.position(0);
    assertTrue(buffer.hasRemaining());

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }
  }

  @Test
  public void testFlip() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    buffer.mark();

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }

    assertFalse(buffer.hasRemaining());
    buffer.flip();
    assertTrue(buffer.hasRemaining());

    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], buffer.get());
    }
  }

  @Test
  public void testReadUTF8() throws CharacterCodingException {
    String testString = "test-string-1";
    byte[] asUtf8bytes = testString.getBytes(StandardCharsets.UTF_8);
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(asUtf8bytes);

    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(testString, buffer.readUTF8());
    assertFalse(buffer.hasRemaining());
  }

  @Test
  public void testReadString() throws CharacterCodingException {
    String testString = "test-string-1";
    byte[] asUtf8bytes = testString.getBytes(StandardCharsets.UTF_8);
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(asUtf8bytes);

    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertEquals(testString, buffer.readString(StandardCharsets.UTF_8.newDecoder()));
    assertFalse(buffer.hasRemaining());
  }

  @Test
  public void testArrayOffset() {
    byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    ByteBuf byteBuffer = Unpooled.wrappedBuffer(data, 5, 5);
    ProtonReadableBufferImpl buffer = new ProtonReadableBufferImpl(byteBuffer);

    assertTrue(buffer.hasArray());
    assertSame(buffer.array(), byteBuffer.array());
    assertEquals(buffer.arrayOffset(), byteBuffer.arrayOffset());

    assertEquals(5, buffer.get());

    assertEquals(buffer.arrayOffset(), byteBuffer.arrayOffset());
  }
}
