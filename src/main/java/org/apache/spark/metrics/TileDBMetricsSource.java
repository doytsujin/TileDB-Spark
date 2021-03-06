package org.apache.spark.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.log4j.Logger;
import org.apache.spark.metrics.source.Source;

/** Base class for metrics source. Initializes all timers used to track function calls */
public class TileDBMetricsSource implements Source {
  public static final String sourceName = "tiledb";

  // Read metrics
  public static final String queryReadTimerTaskName = "query-read-task";
  public static final String queryReadTimerName = "query-read-start-to-close";
  public static final String tileDBReadQuerySubmitTimerName = "tiledb-read-query-submit";
  public static final String queryInitTimerName = "query-init";
  public static final String queryAllocBufferTimerName = "query-alloc-buffers";
  public static final String queryGetScalarAttributeTimerName = "query-get-scalar-attribute";
  public static final String queryGetVariableLengthAttributeTimerName =
      "query-get-variable-length-attribute";
  public static final String queryGetDimensionTimerName = "query-get-dimension";
  public static final String queryCloseNativeArraysTimerName = "query-close-native-arrays";
  public static final String queryNextTimerName = "query-next";
  public static final String queryGetTimerName = "query-get";

  // Data source metrics
  public static final String dataSourceReadSchemaTimerName = "data-source-read-schema";
  public static final String dataSourcePruneColumnsTimerName = "data-source-prune-columns";
  public static final String dataSourcePushFiltersTimerName = "data-source-push-filters";
  public static final String dataSourcePlanBatchInputPartitionsTimerName =
      "data-source-plan-batch-input-partitions";
  public static final String dataSourceBuildRangeFromFilterTimerName =
      "data-source-build-range-from-filter";
  public static final String dataSourceCheckAndMergeRangesTimerName =
      "data-source-check-and-merge-ranges";
  public static final String dataSourceComputeNeededSplitsToReduceToMedianVolumeTimerName =
      "data-source-computer-needed-splits";

  // Write metrics
  public static final String queryWriteTaskTimerName = "query-write-task";
  public static final String queryWriteTimerName = "query-write-start-to-close";
  public static final String queryResetWriteQueryAndBuffersTimerName =
      "query-write-reset-query-and-buffer";
  public static final String queryWriteRecordToBufferTimerName = "query-write-record-to-buffer";
  public static final String queryWriteRowTimerName = "query-write-row";
  public static final String queryWriteFlushBuffersTimerName = "query-write-flush-buffers";
  public static final String queryWriteCloseTileDBResourcesTimerName = "query-write-close";
  public static final String queryWriteCommitTimerName = "query-write-commit";
  private MetricRegistry metricRegistry;

  static Logger log = Logger.getLogger(TileDBMetricsSource.class.getName());

  /** Constructor called by spark when initializing the metrics */
  public TileDBMetricsSource() {
    log.info("Creating TileDBMetricsSource");
    metricRegistry = new MetricRegistry();

    // Read metrics
    metricRegistry.timer(queryReadTimerName);
    metricRegistry.timer(tileDBReadQuerySubmitTimerName);
    metricRegistry.timer(queryInitTimerName);
    metricRegistry.timer(queryAllocBufferTimerName);
    metricRegistry.timer(queryGetScalarAttributeTimerName);
    metricRegistry.timer(queryGetVariableLengthAttributeTimerName);
    metricRegistry.timer(queryGetDimensionTimerName);
    metricRegistry.timer(queryCloseNativeArraysTimerName);
    metricRegistry.timer(queryNextTimerName);
    metricRegistry.timer(queryGetTimerName);

    // Data source metrics
    metricRegistry.timer(dataSourceReadSchemaTimerName);
    metricRegistry.timer(dataSourcePruneColumnsTimerName);
    metricRegistry.timer(dataSourcePushFiltersTimerName);
    metricRegistry.timer(dataSourcePlanBatchInputPartitionsTimerName);
    metricRegistry.timer(dataSourceBuildRangeFromFilterTimerName);
    metricRegistry.timer(dataSourceCheckAndMergeRangesTimerName);
    metricRegistry.timer(dataSourceComputeNeededSplitsToReduceToMedianVolumeTimerName);

    // Write metrics
    metricRegistry.timer(queryWriteTimerName);
    metricRegistry.timer(queryWriteTaskTimerName);
    metricRegistry.timer(queryResetWriteQueryAndBuffersTimerName);
    metricRegistry.timer(queryWriteRecordToBufferTimerName);
    metricRegistry.timer(queryWriteRowTimerName);
    metricRegistry.timer(queryWriteFlushBuffersTimerName);
    metricRegistry.timer(queryWriteCloseTileDBResourcesTimerName);
    metricRegistry.timer(queryWriteCommitTimerName);
  }

  @Override
  public String sourceName() {
    return sourceName;
  }

  /**
   * Helper function for dynamically registering functions, not sure if this is actually working
   *
   * @param timerName time name for later lookup and reporting
   * @return timer instance
   */
  public Timer registerTimer(String timerName) {
    return metricRegistry.timer(timerName);
  }

  /** Return the drop wizard metric registry */
  @Override
  public MetricRegistry metricRegistry() {
    return metricRegistry;
  }
}
