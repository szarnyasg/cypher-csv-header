package hu.bme.mit.cch;

import java.util.Optional;
import java.util.regex.Pattern;

public class Constants {

  //public static final Pattern FIELD_PATTERN = Pattern.compile("^(?<name>\\w*):(?<fieldtype>\\w+)(\\(?<id_space>\\w+\\))?(?<array>\\[\\])?$");
  public static final Pattern FIELD_PATTERN = Pattern.compile("^(?<name>\\w*):(?<fieldtype>\\w+)(\\((?<idspace>\\w+)\\))?(?<array>\\[\\])?$");

  public static final String IGNORE_FIELD = "IGNORE";
  public static final String ID_FIELD = "ID";
  public static final String START_ID_FIELD = "START_ID";
  public static final String END_ID_FIELD = "END_ID";

  public static final String INTERNAL_PREFIX = "csv_internal_";
  public static final String ID_PROPERTY       = INTERNAL_PREFIX + ID_FIELD.toLowerCase();
  public static final String START_ID_PROPERTY = INTERNAL_PREFIX + START_ID_FIELD.toLowerCase();
  public static final String END_ID_PROPERTY   = INTERNAL_PREFIX + END_ID_FIELD.toLowerCase();

  public static final String LINE_VAR = "line";

  public static String getPostfix(Optional<String> idSpace) {
    return ID_PROPERTY + (idSpace.isPresent() ? ("_" + idSpace.get()) : "");
  }

}
