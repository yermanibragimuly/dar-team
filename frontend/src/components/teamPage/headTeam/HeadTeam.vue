<script setup lang="ts">

</script>

<template>
  <div class="flex flex-col w-full">
    <div class="text-white flex justify-center items-center py-12 px-4 sm:px-6 lg:px-12">
      <h2 class="text-3xl font-extrabold uppercase mb-10 text-center tracking-wider">
        {{ $t('lable.ourTeam') }}
      </h2>
    </div>
    <div>
      <div class="mx-[20%]">
        <nav class="">
          <ul class="flex space-x-10">
            <li>
              <router-link 
                to="/teams/fighters" 
                class="
                text-white hover:text-gray-300 transition-colors 
                  font-medium pb-2 cursor-pointer uppercase"
                active-class="text-blue-600 border-b-2 border-red-600"
              >
                {{ $t('lable.fighters') }}
              </router-link>
            </li>
            <li>
              <router-link 
                to="/teams/coaches" 
                class="
                text-white hover:text-gray-300 transition-colors 
                  font-medium pb-2 cursor-pointer uppercase"
                active-class="text-blue-600 border-b-2 border-red-600"
              >
                {{ $t('lable.coaches') }}
              </router-link>
            </li>
            <li>
              <router-link 
                to="/teams/video" 
                class="
                text-white hover:text-gray-300 transition-colors 
                  font-medium pb-2 cursor-pointer uppercase"
                active-class="text-blue-600 border-b-2 border-red-600"
              >
                {{ $t('lable.video') }}
              </router-link>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>