package com.huzt.data;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SqlBlood {
    public List<BloodNode> nodes = new ArrayList<>();
    public Set<BloodEdge> edges =  new HashSet<>();

    public SqlBlood() {
    }
    public SqlBlood(List<BloodNode> nodes, Set<BloodEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<BloodNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<BloodNode> nodes) {
        this.nodes = nodes;
    }

    public Set<BloodEdge> getEdges() {
        return edges;
    }

    public void setEdges(Set<BloodEdge> edges) {
        this.edges = edges;
    }

    @Override
    public String toString() {
        return "TableBlood [nodes=" + nodes + ", edges=" + edges + "]";
    }

}