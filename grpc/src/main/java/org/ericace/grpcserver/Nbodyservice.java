// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nbodyservice.proto

package org.ericace.grpcserver;

public final class Nbodyservice {
  private Nbodyservice() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_CurrentConfig_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_CurrentConfig_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_BodyDescription_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_BodyDescription_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_ModBodyMessage_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_ModBodyMessage_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_ItemCount_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_ItemCount_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_Factor_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_Factor_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_RestitutionCoefficient_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_RestitutionCoefficient_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_nbodyservice_ResultCode_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_nbodyservice_ResultCode_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\022nbodyservice.proto\022\014nbodyservice\032\033goog" +
      "le/protobuf/empty.proto\"\222\001\n\rCurrentConfi" +
      "g\022\016\n\006bodies\030\001 \001(\003\022\031\n\021result_queue_size\030\002" +
      " \001(\003\022\033\n\023computation_threads\030\003 \001(\003\022\030\n\020smo" +
      "othing_factor\030\004 \001(\002\022\037\n\027restitution_coeff" +
      "icient\030\006 \001(\002\"\343\002\n\017BodyDescription\022\t\n\001x\030\001 " +
      "\001(\002\022\t\n\001y\030\002 \001(\002\022\t\n\001z\030\003 \001(\002\022\n\n\002vx\030\004 \001(\002\022\n\n" +
      "\002vy\030\005 \001(\002\022\n\n\002vz\030\006 \001(\002\022\014\n\004mass\030\007 \001(\002\022\016\n\006r" +
      "adius\030\010 \001(\002\022\016\n\006is_sun\030\t \001(\010\022?\n\022collision" +
      "_behavior\030\n \001(\0162#.nbodyservice.Collision",
      "BehaviorEnum\022/\n\nbody_color\030\013 \001(\0162\033.nbody" +
      "service.BodyColorEnum\022\023\n\013frag_factor\030\014 \001" +
      "(\002\022\021\n\tfrag_step\030\r \001(\002\022\026\n\016with_telemetry\030" +
      "\016 \001(\010\022\014\n\004name\030\017 \001(\t\022\r\n\005class\030\020 \001(\t\022\016\n\006pi" +
      "nned\030\021 \001(\010\"D\n\016ModBodyMessage\022\n\n\002id\030\001 \001(\003" +
      "\022\014\n\004name\030\002 \001(\t\022\r\n\005class\030\003 \001(\t\022\t\n\001p\030\004 \003(\t" +
      "\"\037\n\tItemCount\022\022\n\nitem_count\030\001 \001(\003\"\030\n\006Fac" +
      "tor\022\016\n\006factor\030\001 \001(\002\"9\n\026RestitutionCoeffi" +
      "cient\022\037\n\027restitution_coefficient\030\001 \001(\002\"\200" +
      "\001\n\nResultCode\022<\n\013result_code\030\001 \001(\0162\'.nbo",
      "dyservice.ResultCode.ResultCodeEnum\022\017\n\007m" +
      "essage\030\002 \001(\t\"#\n\016ResultCodeEnum\022\006\n\002OK\020\000\022\t" +
      "\n\005ERROR\020\001*T\n\025CollisionBehaviorEnum\022\t\n\005UN" +
      "DEF\020\000\022\010\n\004NONE\020\001\022\013\n\007SUBSUME\020\002\022\013\n\007ELASTIC\020" +
      "\003\022\014\n\010FRAGMENT\020\004*\307\001\n\rBodyColorEnum\022\013\n\007NOC" +
      "OLOR\020\000\022\n\n\006RANDOM\020\001\022\t\n\005BLACK\020\002\022\t\n\005WHITE\020\003" +
      "\022\014\n\010DARKGRAY\020\004\022\010\n\004GRAY\020\005\022\r\n\tLIGHTGRAY\020\006\022" +
      "\007\n\003RED\020\007\022\t\n\005GREEN\020\010\022\010\n\004BLUE\020\t\022\n\n\006YELLOW\020" +
      "\n\022\013\n\007MAGENTA\020\013\022\010\n\004CYAN\020\014\022\n\n\006ORANGE\020\r\022\t\n\005" +
      "BROWN\020\016\022\010\n\004PINK\020\0172\343\004\n\014NBodyService\022L\n\025Se",
      "tComputationThreads\022\027.nbodyservice.ItemC" +
      "ount\032\030.nbodyservice.ResultCode\"\000\022I\n\022SetR" +
      "esultQueueSize\022\027.nbodyservice.ItemCount\032" +
      "\030.nbodyservice.ResultCode\"\000\022@\n\014SetSmooth" +
      "ing\022\024.nbodyservice.Factor\032\030.nbodyservice" +
      ".ResultCode\"\000\022]\n\031SetRestitutionCoefficie" +
      "nt\022$.nbodyservice.RestitutionCoefficient" +
      "\032\030.nbodyservice.ResultCode\"\000\022C\n\014RemoveBo" +
      "dies\022\027.nbodyservice.ItemCount\032\030.nbodyser" +
      "vice.ResultCode\"\000\022D\n\007AddBody\022\035.nbodyserv",
      "ice.BodyDescription\032\030.nbodyservice.Resul" +
      "tCode\"\000\022C\n\007ModBody\022\034.nbodyservice.ModBod" +
      "yMessage\032\030.nbodyservice.ResultCode\"\000\022I\n\020" +
      "GetCurrentConfig\022\026.google.protobuf.Empty" +
      "\032\033.nbodyservice.CurrentConfig\"\000B%\n\026org.e" +
      "ricace.grpcserverP\001\242\002\010NBODYSVCb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.EmptyProto.getDescriptor(),
        }, assigner);
    internal_static_nbodyservice_CurrentConfig_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_nbodyservice_CurrentConfig_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_CurrentConfig_descriptor,
        new java.lang.String[] { "Bodies", "ResultQueueSize", "ComputationThreads", "SmoothingFactor", "RestitutionCoefficient", });
    internal_static_nbodyservice_BodyDescription_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_nbodyservice_BodyDescription_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_BodyDescription_descriptor,
        new java.lang.String[] { "X", "Y", "Z", "Vx", "Vy", "Vz", "Mass", "Radius", "IsSun", "CollisionBehavior", "BodyColor", "FragFactor", "FragStep", "WithTelemetry", "Name", "Class_", "Pinned", });
    internal_static_nbodyservice_ModBodyMessage_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_nbodyservice_ModBodyMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_ModBodyMessage_descriptor,
        new java.lang.String[] { "Id", "Name", "Class_", "P", });
    internal_static_nbodyservice_ItemCount_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_nbodyservice_ItemCount_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_ItemCount_descriptor,
        new java.lang.String[] { "ItemCount", });
    internal_static_nbodyservice_Factor_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_nbodyservice_Factor_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_Factor_descriptor,
        new java.lang.String[] { "Factor", });
    internal_static_nbodyservice_RestitutionCoefficient_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_nbodyservice_RestitutionCoefficient_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_RestitutionCoefficient_descriptor,
        new java.lang.String[] { "RestitutionCoefficient", });
    internal_static_nbodyservice_ResultCode_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_nbodyservice_ResultCode_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_nbodyservice_ResultCode_descriptor,
        new java.lang.String[] { "ResultCode", "Message", });
    com.google.protobuf.EmptyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
