/**
 *
 * Copyright 2017 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.github.evenjn.yarn.Rook;

public class FileFoolElement implements
		FileFoolReaderElement,
		FileFoolWriterElement {

	private final boolean read_only;

	FileFoolElement(Path id, boolean read_only) {
		path = id;
		this.read_only = read_only;
	}

	Path path;

	public InputStream read( Rook rook ) {
		try {
			return rook.hook( Files.newInputStream( path ) );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public OutputStream write( Rook rook ) {
		if ( read_only ) {
			throw new IllegalStateException(
					"You don't have permission to write on this file: "
							+ path.toString( ) );
		}
		try {
			return rook.hook( Files.newOutputStream( path ) );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
}
