<template>
  <ContentList api="/content/exam" title="🎯 高考冲锋专栏" subtitle="冲刺阶段，心态就是实力"
               filter-param="type" :filters="['冲刺心理', '减压技巧', '备考策略']" />
</template>

<script setup>
import ContentList from '../../components/ContentList.vue'
</script>
