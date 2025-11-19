package com.dasi.types.common;

@SuppressWarnings("unused")
public class Constants {

    public static final String COMMA = ",";
    public static final String BLANK = "\\s+";
    public static final String COLON = ":";
    public static final String UNDERSCORE = "_";

    public static class RuleModel {
        public static String RULE_WEIGHT = "rule_weight";
        public static String RULE_BLACKLIST = "rule_blacklist";
        public static String RULE_LUCK_AWARD = "rule_luck_award";
        public static String RULE_LOCK = "rule_lock";
        public static String RULE_RANDOM = "rule_random";
    }

    public static class RedisKey {

        public static String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        public static String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        public static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";

        public static String STRATEGY_KEY = "big_market_strategy_key_";

    }
}
