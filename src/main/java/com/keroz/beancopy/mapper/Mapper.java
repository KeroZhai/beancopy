package com.keroz.beancopy.mapper;

public interface Mapper<Source, Target> {

    Target map(Source source);

}
