package test;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlReplaceTest {


    @Test
    public void test2() {
        String Ids = "3146748814963614565,3146748832143491980,3146750515770674101,3146774000651848168,3146798877102434289,3223410837281204930,3223418104365869399,3223418568222336986,3223449938663441724,3223450935095869758,3223499760284100805,3223501272112568427,3223503797553358499,3223504605007212554,3223563033742306961,3223567569227764482,3223573736800780454,3223598802229946364,3223607890380745774,3223655787856012962,3223659670506461612,3223727256111809986,3223755860594016295,3223758643732808099,3223794326321122887,3223812829040242972,3223983974897053103,3224318862087038613,3224502068212018121,3224747396743975940,3224823658183262643,3224826011825345809,3224826097724706213,3224891673285384309,3224895092079330636,3225113534116007764,3225114221310779667,3225208057756255097,3225238328685772950,3225247897872913785,3225338006286779283,3225417720879786798,3225476080895410876,3225479259171204786,3225481904871063485,3225482454626867979,3225510887310380831,3225512124260960022,3225616148368868672,3225646144420446408,3225646694176268727,3225688922294737087,3225719279123567294,3225761833659550387,3225786280613397015,3225789338630104715,3225789991465146522,3225796657254380715,3225858092466572868,3225898035662439754,3225903516040693884,3225989690264527613,3226000874359374277,3226090656355715282,3226097390864449920,3226159839688920202,3226162416669307804,3226243591551197773,3226244622343342896,3226337668514841995,3226374089837513129,3226426299459968985,3226430972384383274,3256679516517076015,3256734354659515302,3256830493207462712,3256987018995598280,3257060566015577759,3257212504778643801,3257227743322609477,3257229564388742334,3257252001297896981,3257325050101664299,3257430946815317320,3257530452617626093,3257671671142319138,3257710171229161260,3257804213833074626,3257815741525296353,3257869428616500801,3257970325988213104,3258119103655347861,3258291211584832134,3258409804221810187,3258409821401679445";
        String[] split = Ids.split(",");
        for (String s : split) {
            HttpRequest request = HttpUtil.createRequest(Method.valueOf("POST"), "https://cqstt.sxcq2015.com/stt-oms/app/so/cancel/" + s);
            request.header("token", "eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlbklkIjozMjI1NzI3MjY3NzYyNzM0NDMwLCJpZCI6MTIzMDQsInVzZXJUeXBlIjoxMDAsImlhdCI6MTY2ODY1MTgwNSwiZXhwIjoxNjY4ODI0NjA1fQ.Fe664a5LG4CzM2NzlwX6nzdFsu62Oq9CGaHKHilH9QE");
            HttpResponse httpResponse = request.execute();
            System.out.println(httpResponse.getStatus());
        }
    }

    @Test
    public void test5() {
        System.out.println(RandomUtil.randomString(5));
        System.out.println(RandomUtil.randomString(0));
        System.out.println(RandomUtil.randomString("xx", 5));
        System.out.println(LocalDate.now(ZoneId.of(JSON.defaultTimeZone.getID())).toString());
        System.out.println(RandomUtil.randomString(0));
    }

    public static CustomNode convertJsonObjectToNode(CustomNode node, JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        keys.parallelStream().forEach(key -> {

            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                JSONObject valueJsonObject = (JSONObject) value;
                CustomNode customNode = new CustomNode(key, null);
                node.add(convertJsonObjectToNode(customNode, valueJsonObject));
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                if (jsonArray.size() == 0) {
                    return;
                }
                convertJsonArrayToNode(key, jsonArray, node);
            } else {
                node.add(new CustomNode(key, value));
            }
        });
        return node;
    }


    public static void convertJsonArrayToNode(String key, JSONArray jsonArray, CustomNode node) {

        jsonArray.parallelStream().forEach(json -> {
            CustomNode nodeArray = new CustomNode(key, null);
            if (json instanceof JSONObject) {
                JSONObject valueJsonObject = (JSONObject) json;
                node.add(convertJsonObjectToNode(nodeArray, valueJsonObject));
            } else if (json instanceof JSONArray) {
                JSONArray tmpJsonArray = (JSONArray) json;
                if (tmpJsonArray.size() == 0) {
                    return;
                }
                convertJsonArrayToNode(key, tmpJsonArray, nodeArray);
                node.add(nodeArray);
            } else {
                node.add(new CustomNode(key, json));
            }
        });
    }


    public static class CustomNode extends DefaultMutableTreeNode {
        private String key;
        private Object value;

        public CustomNode() {
        }

        public CustomNode(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    private enum SearchTypeEnum {
        name,
        url;

        public static SearchTypeEnum fromValue(String name) {
            for (SearchTypeEnum v : values()) {
                if (v.name().equals(name)) {
                    return v;
                }
            }
            return null;
        }
    }

    private static Map<String, String> getQuery(String search) {
        String[] split = search.split("\\|");
        StringBuilder rule = new StringBuilder();
        String query = "";
        for (String s : split) {
            s = s.trim();
            if (SearchTypeEnum.fromValue(s) != null && search.indexOf("|", search.indexOf(s) + 1) != -1) {
                rule.append(s).append(",");
            } else {
                query = s;
            }
        }
        return ImmutableMap.<String, String>builder().put("query", query).put("rule", rule.toString()).build();
    }

    @Test
    public void test6() {

        System.out.println(getQuery("name| a "));


    }


}
