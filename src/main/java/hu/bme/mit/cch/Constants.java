package hu.bme.mit.cch;

import java.util.Optional;
import java.util.regex.Pattern;

public class Constants {

  //public static final Pattern FIELD_PATTERN = Pattern.compile("^(?<name>\\w*):(?<fieldtype>\\w+)(\\(?<id_space>\\w+\\))?(?<array>\\[\\])?$");
  public static final Pattern FIELD_PATTERN = Pattern.compile("^(?<name>[^:]*):(?<fieldtype>\\w+)(\\((?<idspace>\\w+)\\))?(?<array>\\[\\])?$");
  public static final String ARRAY_PATTERN = "[]";

  public static final String IGNORE_FIELD = "IGNORE";
  public static final String ID_FIELD = "ID";
  public static final String START_ID_FIELD = "START_ID";
  public static final String END_ID_FIELD = "END_ID";

  public static final String IDSPACE_LABEL_PREFIX = "IdSpace";
  public static final String IDSPACE_ATTR_PREFIX = "__csv_id";
  public static final String ID_PROPERTY       = IDSPACE_ATTR_PREFIX + ID_FIELD.toLowerCase();
  public static final String START_ID_PROPERTY = IDSPACE_ATTR_PREFIX + START_ID_FIELD.toLowerCase();
  public static final String END_ID_PROPERTY   = IDSPACE_ATTR_PREFIX + END_ID_FIELD.toLowerCase();

  public static final String LINE_VAR = "line";
}
