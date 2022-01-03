package com.lesfurets.jenkins.unit.declarative

enum ComparatorEnum {

    EQUALS ('EQUALS'),
    GLOB ('GLOB'),
    REGEXP ('REGEXP')

    private static Map map

    static {
        map = [:]
        values().each { comparator ->
            map.put(comparator.name, comparator)
        }
    }

    static ComparatorEnum getComparator(String name) {
        return map.get(name)
    }

    private final String name

    private ComparatorEnum(String name) {
        this.name = name
    }

    @Override
    String toString() {
        return this.name
    }

}