// Copyright 2015 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.profiler.chart;

import com.google.common.base.Joiner;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.devtools.build.lib.profiler.ProfileInfo;
import com.google.devtools.build.lib.profiler.ProfileInfo.Task;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * Computes various statistics for Skylark and built-in function usage and prints it to a given
 * {@link PrintStream}.
 */
public final class SkylarkStatistics {

  /**
   * How many characters from the end of the location of a Skylark function to display.
   */
  private static final int NUM_LOCATION_CHARS_UNABBREVIATED = 40;
  private final ListMultimap<String, Task> userFunctionTasks;
  private final ListMultimap<String, Task> builtinFunctionTasks;
  private final List<TasksStatistics> userFunctionStats;
  private final List<TasksStatistics> builtinFunctionStats;
  private long userTotalNanos;
  private long builtinTotalNanos;

  private final PrintStream out;

  public SkylarkStatistics(PrintStream out, ProfileInfo info) {
    this.out = out;
    userFunctionTasks = info.getSkylarkUserFunctionTasks();
    builtinFunctionTasks = info.getSkylarkBuiltinFunctionTasks();
    userFunctionStats = new ArrayList<>();
    builtinFunctionStats = new ArrayList<>();
    computeStatistics();
  }

  /**
   * For each Skylark function compute a {@link TasksStatistics} object from the execution times of
   * all corresponding {@link Task}s from either {@link #userFunctionTasks} or
   * {@link #builtinFunctionTasks}. Fills fields {@link #userFunctionStats} and
   * {@link #builtinFunctionStats}.
   */
  private void computeStatistics() {
    userTotalNanos = computeStatistics(userFunctionTasks, userFunctionStats);
    builtinTotalNanos = computeStatistics(builtinFunctionTasks, builtinFunctionStats);
  }

  /**
   * For each Skylark function compute a {@link TasksStatistics} object from the execution times of
   * all corresponding {@link Task}s and add it to the list.
   * @param tasks Map from function name to all corresponding tasks.
   * @param stats The list to which {@link TasksStatistics} are to be added.
   * @return The sum of the execution times of all {@link Task} values in the map.
   */
  private static long computeStatistics(
      ListMultimap<String, Task> tasks, List<TasksStatistics> stats) {
    long total = 0L;
    for (Entry<String, List<Task>> entry : Multimaps.asMap(tasks).entrySet()) {
      TasksStatistics functionStats = TasksStatistics.create(entry.getKey(), entry.getValue());
      stats.add(functionStats);
      total += functionStats.totalNanos;
    }
    return total;
  }

  /**
   * Prints all CSS definitions and JavaScript code. May be a large amount of output.
   */
  void printHtmlHead() {
    out.println("<style type=\"text/css\"><!--");
    out.println("div.skylark-histogram {");
    out.println("  width: 95%; margin: 0 auto; display: none;");
    out.println("}");
    out.println("div.skylark-chart {");
    out.println("  width: 100%; height: 200px; margin: 0 auto 2em;");
    out.println("}");
    out.println("div.skylark-table {");
    out.println("  width: 95%; margin: 0 auto;");
    out.println("}");
    out.println("--></style>");

    out.println("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
    out.println("<script type=\"text/javascript\">");
    out.println("google.load(\"visualization\", \"1.1\", {packages:[\"corechart\",\"table\"]});");
    out.println("google.setOnLoadCallback(drawVisualization);");

    String dataVar = "data";
    String tableVar = dataVar + "Table";
    out.printf("var %s = {};\n", dataVar);
    out.printf("var %s = {};\n", tableVar);
    out.println("var histogramData;");

    out.println("function drawVisualization() {");
    printStatsJs(userFunctionStats, "user", dataVar, tableVar, userTotalNanos);
    printStatsJs(builtinFunctionStats, "builtin", dataVar, tableVar, builtinTotalNanos);

    printHistogramData();

    out.println("  document.querySelector('#user-close').onclick = function() {");
    out.println("    document.querySelector('#user-histogram').style.display = 'none';");
    out.println("  };");
    out.println("  document.querySelector('#builtin-close').onclick = function() {");
    out.println("    document.querySelector('#builtin-histogram').style.display = 'none';");
    out.println("  };");
    out.println("};");

    out.println("var options = {");
    out.println("  isStacked: true,");
    out.println("  legend: { position: 'none' },");
    out.println("  hAxis: { },");
    out.println("  histogram: { lastBucketPercentile: 5 },");
    out.println("  vAxis: { title: '# calls',");
    out.println("    viewWindowMode: 'pretty', gridlines: { count: -1 } }");
    out.println("};");

    out.println("function selectHandler(category) {");
    out.println("  return function() {");
    out.printf("    var selection = %s[category].getSelection();\n", tableVar);
    out.println("    if (selection.length < 1) return;");
    out.println("    var item = selection[0];");
    out.printf("    var loc = %s[category].getValue(item.row, 0);\n", dataVar);
    out.printf("    var func = %s[category].getValue(item.row, 1);\n", dataVar);
    out.println("    var key = loc + '#' + func;");
    out.println("    var histData = histogramData[category][key];");
    out.println("    var fnOptions = JSON.parse(JSON.stringify(options));");
    out.println("    fnOptions.title = loc + ' - ' + func;");
    out.println("    var chartDiv = document.getElementById(category+'-chart');");
    out.println("    var chart = new google.visualization.Histogram(chartDiv);");
    out.println("    var histogramDiv = document.getElementById(category+'-histogram');");
    out.println("    histogramDiv.style.display = 'block';");
    out.println("    chart.draw(histData, fnOptions);");
    out.println("  }");
    out.println("};");
    out.println("</script>");
  }

  private void printHistogramData() {
    out.println("  histogramData = {");
    printHistogramData(builtinFunctionTasks, "builtin");
    printHistogramData(userFunctionTasks, "user");
    out.println("  }");
  }

  private void printHistogramData(ListMultimap<String, Task> tasks, String category) {
    out.printf("    '%s': {\n", category);
    for (String function : tasks.keySet()) {
      out.printf("      '%s': google.visualization.arrayToDataTable(\n", function);
      out.print("        [['duration']");
      for (Task task : tasks.get(function)) {
        out.printf(",[%f]", task.duration / 1000000.);
      }
      out.println("],\n        false),");
    }
    out.println("    },");
  }

  private void printStatsJs(
      List<TasksStatistics> statsList,
      String category,
      String dataVar,
      String tableVar,
      long totalNanos) {
    String tmpVar = category + dataVar;
    out.printf("  var statsDiv = document.getElementById('%s_function_stats');\n", category);
    if (statsList.isEmpty()) {
      out.println("  statsDiv.innerHTML = '<i>No relevant function calls to display. Some minor"
          + " builtin functions may have been ignored because their names could not be used as"
          + " variables in JavaScript.</i>'");
    } else {
      out.printf("  var %s = new google.visualization.DataTable();\n", tmpVar);
      out.printf("  %s.addColumn('string', 'Location');\n", tmpVar);
      out.printf("  %s.addColumn('string', 'Function');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'count');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'min (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'mean (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'median (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'max (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'std dev (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'mean self (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'self (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'self (%%)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'total (ms)');\n", tmpVar);
      out.printf("  %s.addColumn('number', 'relative (%%)');\n", tmpVar);
      out.printf("  %s.addRows([\n", tmpVar);
      for (TasksStatistics stats : statsList) {
        double relativeTotal = (double) stats.totalNanos / totalNanos;
        double relativeSelf = (double) stats.selfNanos / stats.totalNanos;
        String[] split = stats.name.split("#");
        String location = split[0];
        String name = split[1];
        out.printf("    [{v:'%s', f:'%s'}, ", location, abbreviatePath(location));
        out.printf("'%s', ", name);
        out.printf("%d, ", stats.count);
        out.printf("%.3f, ", stats.minimumMillis());
        out.printf("%.3f, ", stats.meanMillis());
        out.printf("%.3f, ", stats.medianMillis());
        out.printf("%.3f, ", stats.maximumMillis());
        out.printf("%.3f, ", stats.standardDeviationMillis);
        out.printf("%.3f, ", stats.selfMeanMillis());
        out.printf("%.3f, ", stats.selfMillis());
        out.printf("{v:%.4f, f:'%.3f %%'}, ", relativeSelf, relativeSelf * 100);
        out.printf("%.3f, ", stats.totalMillis());
        out.printf("{v:%.4f, f:'%.3f %%'}],\n", relativeTotal, relativeTotal * 100);
      }
      out.println("  ]);");
      out.printf("  %s.%s = %s;\n", dataVar, category, tmpVar);
      out.printf("  %s.%s = new google.visualization.Table(statsDiv);\n", tableVar, category);
      out.printf(
          "  google.visualization.events.addListener(%s.%s, 'select', selectHandler('%s'));\n",
          tableVar,
          category,
          category);
      out.printf(
          "  %s.%s.draw(%s.%s, {showRowNumber: true, width: '100%%', height: '100%%'});\n",
          tableVar,
          category,
          dataVar,
          category);
    }
  }

  /**
   * Prints two sections for histograms and tables of statistics for user-defined and built-in
   * Skylark functions.
   */
  void printHtmlBody() {
    out.println("<a name='skylark_stats'/>");
    out.println("<h3>Skylark Statistics</h3>");
    out.println("<h4>User-Defined function execution time</h4>");
    out.println("<div class=\"skylark-histogram\" id=\"user-histogram\">");
    out.println("  <div class=\"skylark-chart\" id=\"user-chart\"></div>");
    out.println("  <button id=\"user-close\">Hide histogram</button>");
    out.println("</div>");
    out.println("<div class=\"skylark-table\" id=\"user_function_stats\"></div>");

    out.println("<h4>Builtin function execution time</h4>");
    out.println("<div class=\"skylark-histogram\" id=\"builtin-histogram\">");
    out.println("  <div class=\"skylark-chart\" id=\"builtin-chart\"></div>");
    out.println("  <button id=\"builtin-close\">Hide histogram</button>");
    out.println("</div>");
    out.println("<div class=\"skylark-table\" id=\"builtin_function_stats\"></div>");
  }

  /**
   * Computes a string keeping the structure of the input but reducing the amount of characters on
   * elements at the front if necessary.
   *
   * <p>Reduces the length of function location strings by keeping at least the last element fully
   * intact and at most {@link SkylarkStatistics#NUM_LOCATION_CHARS_UNABBREVIATED} from other
   * elements from the end. Elements before are abbreviated with their first two characters.
   *
   * <p>Example:
   * "//source/tree/with/very/descriptive/and/long/hierarchy/of/directories/longfilename.bzl:42"
   * becomes: "//so/tr/wi/ve/de/an/lo/hierarch/of/directories/longfilename.bzl:42"
   *
   * <p>There is no fixed length to the result as the last element is kept and the location may
   * have many elements.
   *
   * @param location Either a sequence of path elements separated by
   *     {@link StandardSystemProperty#FILE_SEPARATOR} and preceded by some root element
   *     (e.g. "/", "C:\") or path elements separated by "." and having no root element.
   */
  private String abbreviatePath(String location) {
    String[] elements;
    int lowestAbbreviateIndex;
    String root;
    String separator = StandardSystemProperty.FILE_SEPARATOR.value();
    if (location.contains(separator)) {
      elements = location.split(separator);
      // must take care to preserve file system roots (e.g. "/", "C:\"), keep separate
      lowestAbbreviateIndex = 1;
      root = location.substring(0, location.indexOf(separator) + 1);
    } else {
      // must be java class name for a builtin function
      elements = location.split("\\.");
      lowestAbbreviateIndex = 0;
      root = "";
      separator = ".";
    }

    String last = elements[elements.length - 1];
    int remaining = NUM_LOCATION_CHARS_UNABBREVIATED - last.length();
    // start from the next to last element of the location and add until "remaining" many
    // chars added, abbreviate rest with first 2 characters
    for (int index = elements.length - 2; index >= lowestAbbreviateIndex; index--) {
      String element = elements[index];
      if (remaining > 0) {
        int length = Math.min(remaining, element.length());
        element = element.substring(0, length);
        remaining -= length;
      } else {
        element = element.substring(0, Math.min(2, element.length()));
      }
      elements[index] = element;
    }
    return root + Joiner.on(separator).join(Arrays.asList(elements).subList(1, elements.length));
  }
}
