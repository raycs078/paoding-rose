/*
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.scanning.vfs;

import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface FileObject {

    FileName getName() throws IOException;

    FileObject[] getChildren() throws IOException;

    FileType getType() throws IOException;

    URL getURL() throws IOException;

    FileObject getParent() throws IOException;

    FileObject getChild(String name) throws IOException;

    FileContent getContent() throws IOException;

}
