/*
 * Copyright 2021 zjay(darzjay@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zjay.plugin.quickrequest.util;


import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;

public class XmlUtil {
    public static boolean isXml(String str) {
        // 1. 快速失败检查
        if (!isXmlBasic(str)) return false;


        // 2. 解析验证（抽样检查）
        try {
            new SAXParserFactoryImpl().newSAXParser().parse(
                    new ByteArrayInputStream(str.getBytes()),
                    new DefaultHandler() {
                        @Override
                        public void startElement(String uri, String localName,
                                                 String qName, Attributes attributes) throws SAXException {
                            // 仅解析前10个元素
                            if (++count > 10) throw new SAXException("BREAK");
                        }
                        private int count = 0;
                    }
            );
            return true;
        } catch (SAXException e) {
            return "BREAK".equals(e.getMessage()); // 主动中断视为有效
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isXmlBasic(String str) {
        // 1. 空值检查
        if (str == null || str.trim().isEmpty()) return false;

        // 2. 起始字符检查
        int firstLT = str.indexOf('<');
        if (firstLT == -1 || firstLT >= str.length() - 1) return false;

        // 3. 闭合标签特征检查
        int lastGT = str.lastIndexOf('>');
        return lastGT > firstLT;
    }


}
