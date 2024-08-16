package org.example;

import java.util.*;

public class UniversityScheduler {
    static class Class {
        int start;
        int end;
        int size;

        public Class(int start, int end, int size) {
            this.start = start;
            this.end = end;
            this.size = size;
        }
    }

    public static int mostUsedClassroom(int n, int[][] classesInput) {
        List<Class> classes = new ArrayList<>();
        for (int[] c : classesInput) {
            classes.add(new Class(c[0], c[1], c[2]));
        }

        // Sort classes by start time, then by size
        classes.sort((a, b) -> {
            if (a.start != b.start) return a.start - b.start;
            return b.size - a.size; // Larger classes have higher priority
        });

        // Priority queue to manage end times of classes in rooms
        PriorityQueue<int[]> roomQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        int[] classCount = new int[n]; // Count classes in each room

        for (Class c : classes) {
            // Remove rooms that have become free by the start time of the current class
            while (!roomQueue.isEmpty() && roomQueue.peek()[0] <= c.start) {
                roomQueue.poll();
            }

            if (roomQueue.size() < n) {
                // If there's a free room, use it
                int room = roomQueue.size();
                roomQueue.add(new int[]{c.end, room});
                classCount[room]++;
            } else {
                // Delay the class until the earliest room is free
                int[] earliestRoom = roomQueue.poll();
                int newEndTime = earliestRoom[0] + (c.end - c.start);
                roomQueue.add(new int[]{newEndTime, earliestRoom[1]});
                classCount[earliestRoom[1]]++;
            }
        }

        // Find the room with the most classes
        int maxClasses = 0;
        int roomWithMaxClasses = 0;
        for (int i = 0; i < n; i++) {
            if (classCount[i] > maxClasses) {
                maxClasses = classCount[i];
                roomWithMaxClasses = i;
            }
        }

        return roomWithMaxClasses;
    }

    public static void main(String[] args) {
        int n1 = 3;
        int[][] classes1 = {{0, 12, 30}, {2, 7, 20}, {1, 10, 25}, {3, 8, 15}};
        System.out.println(mostUsedClassroom(n1, classes1)); // Output: 0

        int n2 = 4;
        int[][] classes2 = {{2, 15, 30}, {4, 10, 25}, {1, 5, 15}, {3, 12, 10}, {7, 9, 20}};
        System.out.println(mostUsedClassroom(n2, classes2)); // Output: 0

        int n3 = 5;
        int[][] classes3 = {{1, 18, 35}, {5, 14, 30}, {3, 8, 25}, {2, 12, 20}, {6, 10, 15}, {0, 5, 10}};
        System.out.println(mostUsedClassroom(n3, classes3)); // Output: 1
    }
}
