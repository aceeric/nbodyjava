// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nbodyservice.proto

package org.ericace.grpcserver;

/**
 * <pre>
 * Coefficient of restitution
 * </pre>
 *
 * Protobuf type {@code nbodyservice.RestitutionCoefficient}
 */
public  final class RestitutionCoefficient extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:nbodyservice.RestitutionCoefficient)
    RestitutionCoefficientOrBuilder {
  // Use RestitutionCoefficient.newBuilder() to construct.
  private RestitutionCoefficient(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RestitutionCoefficient() {
    restitutionCoefficient_ = 0F;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private RestitutionCoefficient(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 13: {

            restitutionCoefficient_ = input.readFloat();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.ericace.grpcserver.Nbodyservice.internal_static_nbodyservice_RestitutionCoefficient_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.ericace.grpcserver.Nbodyservice.internal_static_nbodyservice_RestitutionCoefficient_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.ericace.grpcserver.RestitutionCoefficient.class, org.ericace.grpcserver.RestitutionCoefficient.Builder.class);
  }

  public static final int RESTITUTION_COEFFICIENT_FIELD_NUMBER = 1;
  private float restitutionCoefficient_;
  /**
   * <code>optional float restitution_coefficient = 1;</code>
   */
  public float getRestitutionCoefficient() {
    return restitutionCoefficient_;
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (restitutionCoefficient_ != 0F) {
      output.writeFloat(1, restitutionCoefficient_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (restitutionCoefficient_ != 0F) {
      size += com.google.protobuf.CodedOutputStream
        .computeFloatSize(1, restitutionCoefficient_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.ericace.grpcserver.RestitutionCoefficient)) {
      return super.equals(obj);
    }
    org.ericace.grpcserver.RestitutionCoefficient other = (org.ericace.grpcserver.RestitutionCoefficient) obj;

    boolean result = true;
    result = result && (
        java.lang.Float.floatToIntBits(getRestitutionCoefficient())
        == java.lang.Float.floatToIntBits(
            other.getRestitutionCoefficient()));
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (37 * hash) + RESTITUTION_COEFFICIENT_FIELD_NUMBER;
    hash = (53 * hash) + java.lang.Float.floatToIntBits(
        getRestitutionCoefficient());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.ericace.grpcserver.RestitutionCoefficient parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.ericace.grpcserver.RestitutionCoefficient prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * Coefficient of restitution
   * </pre>
   *
   * Protobuf type {@code nbodyservice.RestitutionCoefficient}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:nbodyservice.RestitutionCoefficient)
      org.ericace.grpcserver.RestitutionCoefficientOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.ericace.grpcserver.Nbodyservice.internal_static_nbodyservice_RestitutionCoefficient_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.ericace.grpcserver.Nbodyservice.internal_static_nbodyservice_RestitutionCoefficient_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.ericace.grpcserver.RestitutionCoefficient.class, org.ericace.grpcserver.RestitutionCoefficient.Builder.class);
    }

    // Construct using org.ericace.grpcserver.RestitutionCoefficient.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      restitutionCoefficient_ = 0F;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.ericace.grpcserver.Nbodyservice.internal_static_nbodyservice_RestitutionCoefficient_descriptor;
    }

    public org.ericace.grpcserver.RestitutionCoefficient getDefaultInstanceForType() {
      return org.ericace.grpcserver.RestitutionCoefficient.getDefaultInstance();
    }

    public org.ericace.grpcserver.RestitutionCoefficient build() {
      org.ericace.grpcserver.RestitutionCoefficient result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.ericace.grpcserver.RestitutionCoefficient buildPartial() {
      org.ericace.grpcserver.RestitutionCoefficient result = new org.ericace.grpcserver.RestitutionCoefficient(this);
      result.restitutionCoefficient_ = restitutionCoefficient_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.ericace.grpcserver.RestitutionCoefficient) {
        return mergeFrom((org.ericace.grpcserver.RestitutionCoefficient)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.ericace.grpcserver.RestitutionCoefficient other) {
      if (other == org.ericace.grpcserver.RestitutionCoefficient.getDefaultInstance()) return this;
      if (other.getRestitutionCoefficient() != 0F) {
        setRestitutionCoefficient(other.getRestitutionCoefficient());
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.ericace.grpcserver.RestitutionCoefficient parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.ericace.grpcserver.RestitutionCoefficient) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private float restitutionCoefficient_ ;
    /**
     * <code>optional float restitution_coefficient = 1;</code>
     */
    public float getRestitutionCoefficient() {
      return restitutionCoefficient_;
    }
    /**
     * <code>optional float restitution_coefficient = 1;</code>
     */
    public Builder setRestitutionCoefficient(float value) {
      
      restitutionCoefficient_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional float restitution_coefficient = 1;</code>
     */
    public Builder clearRestitutionCoefficient() {
      
      restitutionCoefficient_ = 0F;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:nbodyservice.RestitutionCoefficient)
  }

  // @@protoc_insertion_point(class_scope:nbodyservice.RestitutionCoefficient)
  private static final org.ericace.grpcserver.RestitutionCoefficient DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.ericace.grpcserver.RestitutionCoefficient();
  }

  public static org.ericace.grpcserver.RestitutionCoefficient getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RestitutionCoefficient>
      PARSER = new com.google.protobuf.AbstractParser<RestitutionCoefficient>() {
    public RestitutionCoefficient parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new RestitutionCoefficient(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<RestitutionCoefficient> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RestitutionCoefficient> getParserForType() {
    return PARSER;
  }

  public org.ericace.grpcserver.RestitutionCoefficient getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

