(function () {
	var app = angular.module('myapp', []);

	app.controller('MyappController', function () {
		this.products = gems;
	});

	var gems = [{
			name: 'Louis',
			price: 1.30,
			ok: false,
			hide: false,
			hints: ['good cat!', 'gentle cat']
		},
		{
			name: 'Charles',
			price: 40,
			ok: true,
			hints: ['bad cat!', 'evil cat!']
		},
		{
			name: 'Alice',
			price: 0,
			ok: true,
			hide: true
		}
	];
})();
