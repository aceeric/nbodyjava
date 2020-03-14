// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nbodyservice.proto

package org.ericace.grpcserver;

/**
 * Protobuf enum {@code nbodyservice.BodyColorEnum}
 */
public enum BodyColorEnum
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>NOCOLOR = 0;</code>
   */
  NOCOLOR(0),
  /**
   * <code>RANDOM = 1;</code>
   */
  RANDOM(1),
  /**
   * <code>BLACK = 2;</code>
   */
  BLACK(2),
  /**
   * <code>WHITE = 3;</code>
   */
  WHITE(3),
  /**
   * <code>DARKGRAY = 4;</code>
   */
  DARKGRAY(4),
  /**
   * <code>GRAY = 5;</code>
   */
  GRAY(5),
  /**
   * <code>LIGHTGRAY = 6;</code>
   */
  LIGHTGRAY(6),
  /**
   * <code>RED = 7;</code>
   */
  RED(7),
  /**
   * <code>GREEN = 8;</code>
   */
  GREEN(8),
  /**
   * <code>BLUE = 9;</code>
   */
  BLUE(9),
  /**
   * <code>YELLOW = 10;</code>
   */
  YELLOW(10),
  /**
   * <code>MAGENTA = 11;</code>
   */
  MAGENTA(11),
  /**
   * <code>CYAN = 12;</code>
   */
  CYAN(12),
  /**
   * <code>ORANGE = 13;</code>
   */
  ORANGE(13),
  /**
   * <code>BROWN = 14;</code>
   */
  BROWN(14),
  /**
   * <code>PINK = 15;</code>
   */
  PINK(15),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>NOCOLOR = 0;</code>
   */
  public static final int NOCOLOR_VALUE = 0;
  /**
   * <code>RANDOM = 1;</code>
   */
  public static final int RANDOM_VALUE = 1;
  /**
   * <code>BLACK = 2;</code>
   */
  public static final int BLACK_VALUE = 2;
  /**
   * <code>WHITE = 3;</code>
   */
  public static final int WHITE_VALUE = 3;
  /**
   * <code>DARKGRAY = 4;</code>
   */
  public static final int DARKGRAY_VALUE = 4;
  /**
   * <code>GRAY = 5;</code>
   */
  public static final int GRAY_VALUE = 5;
  /**
   * <code>LIGHTGRAY = 6;</code>
   */
  public static final int LIGHTGRAY_VALUE = 6;
  /**
   * <code>RED = 7;</code>
   */
  public static final int RED_VALUE = 7;
  /**
   * <code>GREEN = 8;</code>
   */
  public static final int GREEN_VALUE = 8;
  /**
   * <code>BLUE = 9;</code>
   */
  public static final int BLUE_VALUE = 9;
  /**
   * <code>YELLOW = 10;</code>
   */
  public static final int YELLOW_VALUE = 10;
  /**
   * <code>MAGENTA = 11;</code>
   */
  public static final int MAGENTA_VALUE = 11;
  /**
   * <code>CYAN = 12;</code>
   */
  public static final int CYAN_VALUE = 12;
  /**
   * <code>ORANGE = 13;</code>
   */
  public static final int ORANGE_VALUE = 13;
  /**
   * <code>BROWN = 14;</code>
   */
  public static final int BROWN_VALUE = 14;
  /**
   * <code>PINK = 15;</code>
   */
  public static final int PINK_VALUE = 15;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static BodyColorEnum valueOf(int value) {
    return forNumber(value);
  }

  public static BodyColorEnum forNumber(int value) {
    switch (value) {
      case 0: return NOCOLOR;
      case 1: return RANDOM;
      case 2: return BLACK;
      case 3: return WHITE;
      case 4: return DARKGRAY;
      case 5: return GRAY;
      case 6: return LIGHTGRAY;
      case 7: return RED;
      case 8: return GREEN;
      case 9: return BLUE;
      case 10: return YELLOW;
      case 11: return MAGENTA;
      case 12: return CYAN;
      case 13: return ORANGE;
      case 14: return BROWN;
      case 15: return PINK;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<BodyColorEnum>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      BodyColorEnum> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<BodyColorEnum>() {
          public BodyColorEnum findValueByNumber(int number) {
            return BodyColorEnum.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return org.ericace.grpcserver.Nbodyservice.getDescriptor()
        .getEnumTypes().get(1);
  }

  private static final BodyColorEnum[] VALUES = values();

  public static BodyColorEnum valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private BodyColorEnum(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:nbodyservice.BodyColorEnum)
}

