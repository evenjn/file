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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

class DeletingFileVisitor
		extends SimpleFileVisitor<Path> {

	private Path except_this_directory;

	DeletingFileVisitor(Path except_this_directory) {
		this.except_this_directory = except_this_directory;
		if ( except_this_directory == null ) {
			throw new IllegalArgumentException(
					"Argument except_this_directory must be non-null." );
		}
	}

	@Override
	public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) {
		try {
			Files.delete( file );
		}
		catch ( IOException e ) {
			new RuntimeException( e );
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed( Path file, IOException exc ) {
		try {
			Files.delete( file );
		}
		catch ( IOException e ) {
			new RuntimeException( e );
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory( Path dir, IOException exc ) {
		if ( exc == null ) {
			if ( !except_this_directory.equals( dir ) ) {
				try {
					Files.delete( dir );
				}
				catch ( IOException e ) {
					new RuntimeException( e );
				}
			}
			return FileVisitResult.CONTINUE;
		}
		else {
			throw new RuntimeException( exc );
		}
	}
}
