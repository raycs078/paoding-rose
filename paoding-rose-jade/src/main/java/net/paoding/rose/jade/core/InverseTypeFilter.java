package net.paoding.rose.jade.core;

import java.io.IOException;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

class InverseTypeFilter implements TypeFilter {

        TypeFilter filter;

        public InverseTypeFilter(TypeFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean match(MetadataReader metadataReader,
                MetadataReaderFactory metadataReaderFactory) throws IOException {
            return !filter.match(metadataReader, metadataReaderFactory);
        }
    }