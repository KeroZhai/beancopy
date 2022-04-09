package com.keroz.morphling.converter;

import com.keroz.morphling.mapper.MapperFactory;

public interface Converter<Source, Target> {

    Target convert(Source source, MapperFactory mapperFactory);

}
