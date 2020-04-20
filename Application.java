package me.game.client;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GLFW.glfwInit();
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		long window = GLFW.glfwCreateWindow(1200, 700, "Vulkan Test", MemoryUtil.NULL, MemoryUtil.NULL);
		
		List<VkPhysicalDevice> list = new ArrayList<VkPhysicalDevice>();
		VkPhysicalDevice dedicatedGPU = null;
		VkPhysicalDevice integratedGPU = null;
		IntBuffer count = MemoryUtil.memAllocInt(1);
		PointerBuffer buffer = MemoryUtil.memAllocPointer(1);
		VkApplicationInfo applicationInfo = VkApplicationInfo.calloc().sType(VK11.VK_STRUCTURE_TYPE_APPLICATION_INFO).pApplicationName(MemoryUtil.memUTF8("LWJGL-Vulkan Test")).applicationVersion(VK11.VK_MAKE_VERSION(1, 1, 0)).pEngineName(MemoryUtil.memUTF8("LWJGL-Vulkan Engine")).engineVersion(VK11.VK_MAKE_VERSION(1, 1, 0)).apiVersion(VK11.VK_API_VERSION_1_1);
		VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.calloc().sType(VK11.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO).pApplicationInfo(applicationInfo).flags(0);
		if(VK11.vkCreateInstance(instanceInfo, null, buffer) != VK11.VK_SUCCESS) {
			System.out.println("Failed to initialise Vulkan! Exiting Application...");
			return;
		}
		VkInstance instance = new VkInstance(buffer.get(0), instanceInfo);
		System.out.println("Vulkan initialised. Details of Vulkan instance: " + instance);
		JOptionPane.showMessageDialog(null, "Vulkan initialised. Details of Vulkan instance: " + instance, "Launch Success", JOptionPane.INFORMATION_MESSAGE);
		
		if(VK11.vkEnumeratePhysicalDevices(instance, count, null) != VK11.VK_SUCCESS) {
			System.out.println("Error encountered in locating Vulkan capable GPU(s). Exiting application...");
			return;
		}
		
		if(count.get(0) <= 0) {
			System.out.println("Could not locate any Vulkan capable GPU. Exiting application...");
		}
		
		System.out.println("GPU count: " + count.get(0));
		
		PointerBuffer gpuIDs = MemoryUtil.memAllocPointer(count.get(0));
		
		if(VK11.vkEnumeratePhysicalDevices(instance, count, gpuIDs) != VK11.VK_SUCCESS) {
			System.out.println("Error encountered in locating Vulkan capable GPU(s). Exiting application...");
			return;
		}
		
		for(int i = 0; i < count.get(0); i++) {
			list.add(new VkPhysicalDevice(gpuIDs.get(i), instance));
		}
		
		for(VkPhysicalDevice vkPhysicalDevice : list) {
			VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc();
			VK11.vkGetPhysicalDeviceProperties(vkPhysicalDevice, properties);
			System.out.println("GPU(s) found: " + list.size());
			System.out.println("Device name: " + properties.deviceNameString());
			if(properties.deviceType() == VK11.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
				dedicatedGPU = vkPhysicalDevice;
				System.out.println("Device " + properties.deviceNameString() + " is a Dedicated Graphics Card.");
			} else if(properties.deviceType() == VK11.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
				integratedGPU = vkPhysicalDevice;
				System.out.println("Device " + properties.deviceNameString() + " is an Integrated Graphics Card. It will be used for non-graphical compute acceleration.");
			} else if(properties.deviceType() == VK11.VK_PHYSICAL_DEVICE_TYPE_CPU) {
				System.out.println("Device " + properties.deviceNameString() + " is a CPU. Why the hell did it show up here??");
			}
		}
		
		if(dedicatedGPU != null) {
			VK11.vkGetPhysicalDeviceQueueFamilyProperties(dedicatedGPU, count, null);
			System.out.println("Property count: " + count.get(0));
			VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.calloc(count.get(0));
			VK11.vkGetPhysicalDeviceQueueFamilyProperties(dedicatedGPU, count, queueFamilies);
			System.out.println("Queue Family Buffer size: " + queueFamilies.remaining());
			int i = 0;
			while(queueFamilies.limit() > i) {
				VkQueueFamilyProperties queueFamily = queueFamilies.get(i);
				System.out.println("Number of Queues: " + queueFamily.queueCount());
				switch(queueFamily.queueFlags()) {
				    case VK11.VK_QUEUE_GRAPHICS_BIT:
				    	System.out.println("Queue Family supports Graphics");
				    	break;
				    case VK11.VK_QUEUE_COMPUTE_BIT:
				    	System.out.println("Queue Family supports Computing");
				    	break;	
				    case VK11.VK_QUEUE_TRANSFER_BIT:
				    	System.out.println("Queue Family supports Transfer operations");
				    	break;	
				    case VK11.VK_QUEUE_SPARSE_BINDING_BIT:
				    	System.out.println("Queue Family supports Sparse Memory Management");
				    	break;	
				    case VK11.VK_QUEUE_PROTECTED_BIT:
				    	System.out.println("What the hell?");
				    	break;
				}
				i++;
			}
		}
		
		while(!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwPollEvents();
		}
		
		VK11.vkDestroyInstance(instance, null);
		
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		
	}

}
