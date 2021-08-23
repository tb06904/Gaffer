/*
 * Copyright 2017-2020 Crown Copyright
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
 */
package uk.gov.gchq.gaffer.spark.serialisation.kryo.impl;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.spark.serialisation.kryo.KryoSerializerTest;
import uk.gov.gchq.gaffer.types.TypeSubTypeValue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeSubTypeValueKryoSerializerTest extends KryoSerializerTest<TypeSubTypeValue> {

    @Test
    @Override
    protected void shouldCompareSerialisedAndDeserialisedObjects(final TypeSubTypeValue obj, final TypeSubTypeValue deserialised) {
        assertEquals(obj, deserialised);
    }

    @Override
    protected Class<TypeSubTypeValue> getTestClass() {
        return TypeSubTypeValue.class;
    }

    @Override
    protected TypeSubTypeValue getTestObject() {
        return new TypeSubTypeValue("type", "subtype", "value");
    }
}
