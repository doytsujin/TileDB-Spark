package io.tiledb.spark;

import static io.tiledb.spark.util.generateAllSubarrays;
import static java.lang.Math.ceil;

import io.tiledb.java.api.TileDBError;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SubArrayRanges implements Comparable<SubArrayRanges> {
  private List<Range> ranges;
  private Class datatype;

  SubArrayRanges(List<Range> ranges, Class datatype) {
    this.ranges = ranges;
    this.datatype = datatype;
  }

  public Class getDatatype() {
    return datatype;
  }

  public List<Range> getRanges() {
    return ranges;
  }

  /**
   * Finds the dimension with the largest width
   *
   * @return index of dimension
   */
  public int getDimensionWithLargestWidth() {
    if (datatype == Byte.class) {
      return IntStream.range(0, ranges.size())
          .boxed()
          .max(Comparator.comparing(e -> ranges.get(e).width().byteValue()))
          .get();
      //      ranges.stream()
      //              .max( Comparator.comparing( e -> e.width().byteValue()))
      //              .get();
    } else if (datatype == Short.class) {
      return IntStream.range(0, ranges.size())
          .boxed()
          .max(Comparator.comparing(e -> ranges.get(e).width().shortValue()))
          .get();
    } else if (datatype == Integer.class) {
      return IntStream.range(0, ranges.size())
          .boxed()
          .max(Comparator.comparing(e -> ranges.get(e).width().intValue()))
          .get();
    } else if (datatype == Long.class) {
      return IntStream.range(0, ranges.size())
          .boxed()
          .max(Comparator.comparing(e -> ranges.get(e).width().longValue()))
          .get();
    } else if (datatype == Float.class) {
      return IntStream.range(0, ranges.size())
          .boxed()
          .max(Comparator.comparing(e -> ranges.get(e).width().floatValue()))
          .get();
    } else if (datatype == Double.class) {
      return IntStream.range(0, ranges.size())
          .boxed()
          .max(Comparator.comparing(e -> ranges.get(e).width().doubleValue()))
          .get();
    }

    return 0;
  }

  public <T extends Number> T getVolume() {
    if (datatype == Byte.class) {
      return (T) volumeByte();
    } else if (datatype == Short.class) {
      return (T) volumeShort();
    } else if (datatype == Integer.class) {
      return (T) volumeInteger();
    } else if (datatype == Long.class) {
      return (T) volumeLong();
    } else if (datatype == Float.class) {
      return (T) volumeFloat();
    } else if (datatype == Double.class) {
      return (T) volumeDouble();
    }

    return (T) (Integer) (0);
  }

  private Long volumeByte() {
    long volume = 1;
    for (Range dimRange : ranges) {
      volume *= dimRange.width().longValue();
    }
    return volume;
  }

  private Long volumeShort() {
    long volume = 1;
    for (Range dimRange : ranges) {
      volume *= dimRange.width().longValue();
    }
    return volume;
  }

  private Long volumeInteger() {
    long volume = 1;
    for (Range dimRange : ranges) {
      volume *= dimRange.width().longValue();
    }
    return volume;
  }

  private Long volumeLong() {
    long volume = 1;
    for (Range dimRange : ranges) {
      volume *= dimRange.width().longValue();
    }
    return volume;
  }

  private Double volumeFloat() {
    double volume = 1;
    for (Range dimRange : ranges) {
      volume *= dimRange.width().doubleValue();
    }
    return volume;
  }

  private Double volumeDouble() {
    double volume = 1;
    for (Range dimRange : ranges) {
      volume *= dimRange.width().doubleValue();
    }
    return volume;
  }

  @Override
  public int compareTo(SubArrayRanges other) {
    // compareTo should return < 0 if this is supposed to be
    // less than other, > 0 if this is supposed to be greater than
    // other and 0 if they are supposed to be equal
    if (datatype == Byte.class) {
      return volumeByte().compareTo(other.volumeByte());
    } else if (datatype == Short.class) {
      return volumeShort().compareTo(other.volumeShort());
    } else if (datatype == Integer.class) {
      return volumeInteger().compareTo(other.volumeInteger());
    } else if (datatype == Long.class) {
      return volumeLong().compareTo(other.volumeLong());
    } else if (datatype == Float.class) {
      return volumeFloat().compareTo(other.volumeFloat());
    } else if (datatype == Double.class) {
      return volumeDouble().compareTo(other.volumeDouble());
    }

    return 0;
  }

  public boolean splittable() {
    for (Range range : ranges) {
      if (!range.splittable()) return false;
    }

    return true;
  }

  public List<SubArrayRanges> split(int splits) throws TileDBError {
    List<SubArrayRanges> newSubarrays = new ArrayList<>();

    // Handle base case where there is a single dimension
    if (ranges.size() == 1) {
      if (ranges.get(0).splittable()) {
        for (Range newRange : ranges.get(0).splitRange(splits)) {
          List<Range> dimRanges = new ArrayList<>();
          dimRanges.add(newRange);
          newSubarrays.add(new SubArrayRanges(dimRanges, datatype));
        }
      }
    } else {

      List<Double> dimensionVolumeRatios = computeDimensionVolumeRations();

      List<List<Range>> newSplits = new ArrayList<>();
      // Use volume ratios to weightily determine splits
      for (int i = 0; i < ranges.size(); i++) {
        if (ranges.get(i).splittable()) {

          /*
           The weights are computed based on the width of each range
           For example if we have 3 ranges (dimensions) with width 10, 15 and 35
           the percentages are previously computed to be:
           10 / (10+15+35) = 0.16666, 15/(10+15+35) = 0.25, 35 / (10+15+35) = 0.58333
           We take the total number of splits multiply by the percentage and divide by the number of ranges (dimensions)
           If there is 6 splits, they are computed to be:
           6*0.1666 / 3 = 1, 6*0.25/3 = 1 and 6*0.5833/3 = 2
          */
          int dimensionWeightedSplits =
              (int) ceil(((splits * dimensionVolumeRatios.get(i)) / ranges.size()));

          // Split the given range for a dimension into x splits
          newSplits.add(new ArrayList<>(ranges.get(i).splitRange(dimensionWeightedSplits)));
        }
      }
      generateAllSubarrays(newSplits, newSubarrays, 0, new ArrayList<>());
    }

    return newSubarrays;
  }

  /**
   * Compute the percentage of width that each dimension gives based on the total summation of
   * widths For example if we have 3 ranges with widths 10, 15 and 35 then the computation is: 10 /
   * (10+15+35) = 0.16666, 15/(10+15+35) = 0.25, 35 / (10+15+35) = 0.58333
   *
   * @return
   */
  private List<Double> computeDimensionVolumeRations() {
    List<Object> widths = ranges.stream().map(Range::width).collect(Collectors.toList());
    if (datatype == Byte.class) {
      long sum = widths.stream().map(e -> (Byte) e).mapToLong(Byte::longValue).sum();
      return widths.stream().map(e -> ((Byte) e).doubleValue() / sum).collect(Collectors.toList());
    } else if (datatype == Short.class) {
      long sum = widths.stream().map(e -> (Short) e).mapToLong(Short::longValue).sum();
      return widths.stream().map(e -> ((Short) e).doubleValue() / sum).collect(Collectors.toList());
    } else if (datatype == Integer.class) {
      long sum = widths.stream().map(e -> (Integer) e).mapToLong(Integer::longValue).sum();
      return widths
          .stream()
          .map(e -> ((Integer) e).doubleValue() / sum)
          .collect(Collectors.toList());
    } else if (datatype == Long.class) {
      long sum = widths.stream().map(e -> (Long) e).mapToLong(Long::longValue).sum();
      return widths.stream().map(e -> ((Long) e).doubleValue() / sum).collect(Collectors.toList());
    } else if (datatype == Float.class) {
      double sum = widths.stream().map(e -> (Float) e).mapToDouble(Float::doubleValue).sum();
      return widths.stream().map(e -> ((Float) e).doubleValue() / sum).collect(Collectors.toList());
    }

    // Else assume double
    double sum = widths.stream().map(e -> (Double) e).mapToDouble(Double::doubleValue).sum();
    return widths.stream().map(e -> ((Double) e) / sum).collect(Collectors.toList());
  }
}