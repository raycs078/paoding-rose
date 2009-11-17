/*
 * Copyright 2007-2009 the original author or authors.
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
package net.paoding.rose.web.impl.mapping;

import java.util.Set;

import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.MatchResult;
import net.paoding.rose.web.impl.thread.tree.MappingNode;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 * @param <S>
 * @param <N>
 */
public class ModifiedMapping<S, N> implements Mapping<N> {

    private Mapping<S> src;

    private N newTarget;

    private ModifiedMapping(Mapping<S> src, N newTarget) {
        this.src = src;
        this.newTarget = newTarget;
    }

    public static <S, N> Mapping<N> changeTarget(Mapping<S> src, N newTarget) {
        return new ModifiedMapping<S, N>(src, newTarget);
    }

    @Override
    public N getTarget() {
        return newTarget;
    }

    @Override
    public ReqMethod[] getMethods() {
        return src.getMethods();
    }

    @Override
    public String getPath() {
        return src.getPath();
    }

    @Override
    public int getConstantCount() {
        return src.getConstantCount();
    }

    @Override
    public int getParameterCount() {
        return src.getParameterCount();
    }

    @Override
    public MatchResult<N> match(String path, String requestMethod, MappingNode node) {
        return MatchResult.changeMapping(src.match(path, requestMethod, node), this);
    }

    @Override
    public int compareTo(Mapping<?> o) {
        if (o instanceof ModifiedMapping) {
            return src.compareTo(((ModifiedMapping<?, ?>) o).src);
        }
        return src.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Mapping)) {
            return false;
        }
        return this.compareTo((Mapping<?>) obj) == 0;
    }

    @Override
    public int hashCode() {
        return src.hashCode();
    }

    @Override
    public String toString() {
        return src + " ->target: " + newTarget;
    }

    @Override
    public Set<ReqMethod> getResourceMethods() {
        return src.getResourceMethods();
    }

}
