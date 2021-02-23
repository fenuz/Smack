/**
 *
 * Copyright 2020-2021 Florian Schmaus
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
package org.jivesoftware.smackx.formtypes;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.TextSingleFormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

public class FormFieldRegistry {

    private static final Map<String, Map<String, FormField.Type>> REGISTRY = new HashMap<>();

    @SuppressWarnings("ReferenceEquality")
    public static synchronized void register(DataForm dataForm) {
        // TODO: Also allow forms of type 'result'?
        if (dataForm.getType() != DataForm.Type.form) {
            throw new IllegalArgumentException();
        }

        String formType = null;
        TextSingleFormField hiddenFormTypeField = dataForm.getHiddenFormTypeField();
        if (hiddenFormTypeField == null) {
            throw new IllegalArgumentException("Can only register forms with (hidden) FROM_TYPE field");
        }

        for (FormField formField : dataForm.getFields()) {
            // Note that we can compare here by reference equality to skip the hidden form type field.
            if (formField == hiddenFormTypeField) {
                continue;
            }

            String fieldName = formField.getFieldName();
            FormField.Type type = formField.getType();
            register(formType, fieldName, type);
        }
    }

    public static synchronized void register(String formType, String fieldName, FormField.Type type) {
        Map<String, FormField.Type> fieldNameToType = REGISTRY.get(formType);
        if (fieldNameToType == null) {
            fieldNameToType = new HashMap<>();
            REGISTRY.put(formType, fieldNameToType);
        } else {
            FormField.Type previousType = fieldNameToType.get(fieldName);
            if (previousType != null && previousType != type) {
                throw new IllegalArgumentException();
            }
        }
        fieldNameToType.put(fieldName, type);
    }

    public static synchronized FormField.Type lookup(String formType, String fieldName) {
        if (formType == null) {
            throw new IllegalArgumentException("cannot lookup a field type without a formType");
        }

        Map<String, FormField.Type> fieldNameToTypeMap = REGISTRY.get(formType);
        if (fieldNameToTypeMap != null) {
            FormField.Type type = fieldNameToTypeMap.get(fieldName);
            if (type != null) {
                return type;
            }
        }

        return null;
    }

    public static final class FormFieldInformation {
        public final FormField.Type formFieldType;
        public final String formType;


        private FormFieldInformation(FormField.Type formFieldType, String formType) {
            this.formFieldType = formFieldType;
            this.formType = formType;
        }
    }
}
