/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.issue.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.sonarsource.sonarlint.core.client.api.connected.objectstore.PathMapper;
import org.sonarsource.sonarlint.core.client.api.connected.objectstore.Reader;
import org.sonarsource.sonarlint.core.client.api.connected.objectstore.Writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexedObjectStoreTest {
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private IndexedObjectStore<String, String> store;
  private StoreIndex<String> index;
  private Path root;
  private StoreKeyValidator<String> validator;

  @Before
  public void setUp() {
    root = temp.getRoot().toPath();
    index = mock(StoreIndex.class);
    validator = mock(StoreKeyValidator.class);
    PathMapper<String> mapper = str -> root.resolve(str);
    Reader<String> reader = (stream) -> new Scanner(stream).next();
    Writer<String> writer = (stream, str) -> {
      try {
        stream.write(str.getBytes());
      } catch (IOException e) {
        e.printStackTrace();
      }
    };

    store = new IndexedObjectStore<>(index, mapper, reader, writer, validator);
  }

  @Test
  public void testWrite() throws IOException {
    store.write("mykey", "myvalue");
    assertThat(root.resolve("mykey")).hasContent("myvalue");
    Mockito.verify(index).save("mykey", root.resolve("mykey"));

    assertThat(store.read("mykey").get()).isEqualTo("myvalue");
  }

  @Test
  public void testDelete() throws IOException {
    store.write("mykey", "myvalue");
    assertThat(root.resolve("mykey")).hasContent("myvalue");

    store.delete("mykey");
    assertThat(store.read("mykey").isPresent()).isFalse();
    assertThat(root.resolve("mykey")).doesNotExist();
    Mockito.verify(index).delete("mykey");
  }

  @Test
  public void testClean() throws IOException {
    store.write("mykey", "myvalue");
    store.write("mykey2", "myvalue2");

    when(index.keys()).thenReturn(Arrays.asList("mykey", "mykey2"));
    when(validator.apply("mykey")).thenReturn(Boolean.FALSE);
    when(validator.apply("mykey2")).thenReturn(Boolean.TRUE);

    store.deleteInvalid();
    assertThat(root.resolve("mykey")).doesNotExist();
    assertThat(root.resolve("mykey2")).exists();
    Mockito.verify(index).delete("mykey");
  }
}