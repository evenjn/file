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

import java.nio.file.Path;

public interface FileFoolReader {

	Path getRoot( );

	boolean exists( Path path );

	FileFoolReaderElement open( Path path );

	Iterable<Path> find( String glob_pattern );

	Path normalizedAbsolute( Path path );

	Path normalizedRelative( Path path );
}
