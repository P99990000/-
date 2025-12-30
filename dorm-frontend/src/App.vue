<script setup lang="ts">
import NotificationContainer from './components/NotificationContainer.vue'
</script>

<template>
  <router-view />
  <NotificationContainer />
</template>

<style>
/* Reset some default styles if needed, or keep global styles */
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}
</style>
