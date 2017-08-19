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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

public final class FileFool implements
		FileFoolReader,
		FileFoolWriter {

	private Path root;

	private boolean read_only;

	public Path getRoot( ) {
		return root;
	}

	private FileFool(Path path, boolean read_only) {
		this.root = path;
		this.read_only = read_only;
	}

	private static FileFool internalConstructor( Path path, boolean read_only ) {
		if ( path == null ) {
			throw new IllegalArgumentException( "Argument path must be non-null." );
		}
		if ( !path.isAbsolute( ) ) {
			throw new IllegalArgumentException( "Argument path must be absolute: " + path.toString( ) );
		}
		Path the_path = path.toAbsolutePath( ).normalize( );
		if ( !Files.exists( the_path ) ) {
			throw new IllegalArgumentException(
					"Argument path identifies no existing directory: " + the_path.toString( ) );
		}
		if ( !Files.isDirectory( the_path ) ) {
			throw new IllegalArgumentException(
					"Argument path identifies a file that is not a directory: " + the_path.toString( ) );
		}
		return new FileFool( the_path, read_only );
	}

	/**
	 * @return A read-only FileFool rooted at the root of the file system.
	 */
	public static FileFoolReader r( ) {
		return internalConstructor( Paths.get( "/" ), true );
	}

	@Deprecated
	public static FileFool nu( Path path ) {
		return internalConstructor( path, false );
	}

	public static FileFoolReader r( Path path ) {
		return internalConstructor( path, true );
	}

	public static FileFool rw( Path path ) {
		return internalConstructor( path, false );
	}

	public static FileFoolWriter w( Path path ) {
		return internalConstructor( path, false );
	}

	public boolean exists( Path path ) {
		return Files.exists( resolve( path ) );
	}

	private void checkReadOnly( ) {
		if ( read_only ) {
			throw new IllegalStateException( "This FileFool is read-only." );
		}
	}

	public FileFoolCreation mold( Path path ) {
		checkReadOnly( );
		return new FileFoolCreation( path );
	}

	private Path resolve( Path original_path ) {
		Path the_path;
		if ( original_path.isAbsolute( ) ) {
			the_path = original_path.normalize( );
		}
		else {
			the_path = root.resolve( original_path ).normalize( );
		}
		if ( !the_path.startsWith( root ) ) {
			throw new IllegalStateException(
					"\n This file fool does not allow to access this path:\n "
							+ original_path.toString( ) + "\n The root is:\n "
							+ root.toString( )
							+ "\n" );
		}
		return the_path;
	}
	
	public Path normalizedAbsolute(Path path) {
		return resolve( path );
	}
	
	public Path normalizedRelative(Path path) {
		return root.relativize( path );
	}

	/*
	 * Returns the newly-created file's relative path with respect to the root of
	 * this FileFool.
	 */
	public Path create( FileFoolCreation param ) {
		checkReadOnly( );
		Path the_path = resolve( param.path );
		if ( the_path.equals( root ) ) {
			throw new IllegalStateException( "Path of new file cannot be the root." );
		}
		try {
			if ( param.erase && Files.exists( the_path ) ) {
				internalDelete( the_path );
			}
			if ( param.as_directory ) {
				Files.createDirectories( the_path );
			}
			else {
				Files.createDirectories( the_path.getParent( ) );
				Files.createFile( the_path );
			}
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
		return normalizedRelative( the_path );
	}

	private void internalDelete( Path path ) {
		try {
			Files.walkFileTree( path, new DeletingFileVisitor( root ) );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public void delete( Path path ) {
		checkReadOnly( );
		Path the_path = resolve( path );
		Files.exists( the_path );
		internalDelete( the_path );
	}

	public FileFoolElement open( Path path ) {
		return new FileFoolElement( resolve( path ), read_only );
	}

	/**
	 * @param glob_pattern
	 *          The pattern to match.
	 * @return Paths relative to the root directory, matching the argument
	 *         glob_pattern.
	 */
	public Iterable<Path> find( String glob_pattern ) {
		return find( root, glob_pattern );
	}

	/**
	 * Uses {@linkplain java.nio.file.FileSystem#getPathMatcher(String) glob
	 * syntax}.
	 * 
	 * @param directory
	 * @param glob_pattern
	 *          The pattern to match.
	 * @return Paths relative to the argument directory, matching the argument
	 *         glob_pattern.
	 */
	public static Iterable<Path> find( Path directory, String glob_pattern ) {
		LinkedList<Path> collected = new LinkedList<>( );
		PathMatcher matcher =
				FileSystems.getDefault( ).getPathMatcher( "glob:" + glob_pattern );
		SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>( ) {

			@Override
			public FileVisitResult visitFile( Path file,
					BasicFileAttributes attrs ) {
				Path name =
						directory.toAbsolutePath( ).relativize( file.toAbsolutePath( ) );
				if ( name != null && matcher.matches( name ) ) {
					collected.add( file );
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed( Path file,
					IOException exc ) {
				throw new RuntimeException( exc );
			}
		};
		try {
			Files.walkFileTree( directory, finder );
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
		return collected;
	}

}
