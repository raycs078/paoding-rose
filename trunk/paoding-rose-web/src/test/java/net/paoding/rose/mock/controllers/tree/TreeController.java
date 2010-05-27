package net.paoding.rose.mock.controllers.tree;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.thread.Rose;

@Path("")
public class TreeController {

    @Get("count")
    public int count(Rose rose) {
        MappingNode tree = rose.getMappingTree();
        int count = 0;
        for (MappingNode node : tree) {
            // System.out.print(">" + node);
            if (node.getPath().startsWith("/rose-info")) {
                // System.out.print("\t" + node);
                if (node.getLeftMostChild() == null) {
                    // System.out.print("   YES\t" + node);
                    count++;
                }
            }
            //System.out.println();
        }
        return count;
    }
}
