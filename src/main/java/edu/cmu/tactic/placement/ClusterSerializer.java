package edu.cmu.tactic.placement;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class ClusterSerializer extends JsonSerializer<Cluster> {
	@Override
	public void serialize(Cluster cluster, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		for (Entry<Host, Collection<VirtualMachine>> entry:cluster.getMapping().asMap().entrySet()) {
			jgen.writeArrayFieldStart(entry.getKey().getName());
			for (VirtualMachine vm:entry.getValue()) {
				jgen.writeString(vm.getName());
				/*
				jgen.writeArrayFieldStart(vm.getName());
				for (Component comp:vm.getTenants()) {
					jgen.writeString(comp.getName());
				}
				jgen.writeEndArray();
				*/
			}			
			jgen.writeEndArray();
		}
		jgen.writeEndObject();
	}
	
	@Override
	public Class<Cluster> handledType() {
		return Cluster.class;
	}
}

