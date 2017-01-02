package com.kamesuta.mc.signpic.asm;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.kamesuta.mc.signpic.Log;
import com.kamesuta.mc.signpic.asm.lib.VisitorHelper;
import com.kamesuta.mc.signpic.asm.lib.VisitorHelper.TransformProvider;

import net.minecraft.launchwrapper.IClassTransformer;

public class SignPictureTransformer implements IClassTransformer {

	@Override
	public @Nullable byte[] transform(final @Nullable String name, final @Nullable String transformedName, final @Nullable byte[] bytes) {
		if (bytes==null||name==null||transformedName==null)
			return bytes;

		if (transformedName.equals("net.minecraft.tileentity.TileEntity"))
			return VisitorHelper.apply(bytes, name, new TransformProvider(ClassWriter.COMPUTE_FRAMES) {
				@Override
				public ClassVisitor createVisitor(final String name, final ClassVisitor cv) {
					Log.log.info(String.format("Patching TileEntity.getRenderBoundingBox (class: %s)", name));
					return new TileEntityVisitor(name, cv);
				}
			});

		return bytes;
	}
}